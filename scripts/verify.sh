#!/usr/bin/env bash
# Boots the image and checks the bootstrap really configured Jenkins, then waits
# for the self-triggered seed job to prove the generated pipelines land. Needs network access to
# GIT_PIPELINE_REPO. Usage: make verify
set -euo pipefail

IMAGE=${IMAGE:-joewalker/jenkins-master}
NAME=${NAME:-jenkins-verify}
PORT=${PORT:-18080}
URL=http://localhost:${PORT}
ADMIN_USERNAME=verify_admin
ADMIN_PASSWORD=verify_$RANDOM$RANDOM
GIT_PIPELINE_REPO=${GIT_PIPELINE_REPO:-joecomscience/jenkins-configuration}
HERE=$(cd "$(dirname "$0")" && pwd)

cleanup() { docker rm -f "$NAME" >/dev/null 2>&1 || true; }
trap cleanup EXIT
cleanup

echo "==> booting $IMAGE"
docker run -d --name "$NAME" -p "${PORT}:8080" \
  -e ADMIN_USERNAME="$ADMIN_USERNAME" \
  -e ADMIN_PASSWORD="$ADMIN_PASSWORD" \
  -e GIT_BASE_URL=https://github.com/ \
  -e GIT_USER=verify \
  -e GIT_TOKEN=verify \
  -e GIT_CREDENTIAL_ID=github_credential \
  -e GIT_PIPELINE_REPO="$GIT_PIPELINE_REPO" \
  -e GIT_SHARE_LIB_REPO=joecomscience/jenkins-shared-libs \
  -e JENKINS_URL="$URL" \
  "$IMAGE" >/dev/null

# logs go to a file first: `docker logs | grep -q` makes grep exit early, and the
# SIGPIPE that kills docker logs turns into a non-zero pipeline under pipefail,
# so a match would read as no match
log=$(mktemp)
for _ in $(seq 1 90); do
  docker logs "$NAME" >"$log" 2>&1
  grep -q 'fully up and running' "$log" && break
  sleep 2
done
grep -q 'fully up and running' "$log" || {
  echo "!! jenkins did not start"; tail -40 "$log"; exit 1
}

# a bootstrap step that throws is only visible in the log, the boot still succeeds
if grep -q 'Failed to run script' "$log"; then
  echo "!! a startup script failed"; grep -A20 'Failed to run script' "$log"; exit 1
fi

echo "==> anonymous access is refused"
code=$(curl -s -o /dev/null -w '%{http_code}' "$URL/api/json")
[ "$code" = 403 ] || { echo "!! anonymous got HTTP $code, expected 403"; exit 1; }

auth="${ADMIN_USERNAME}:${ADMIN_PASSWORD}"
jar=$(mktemp)
crumb=$(curl -s -c "$jar" -u "$auth" "$URL/crumbIssuer/api/json" |
  python3 -c 'import sys,json;d=json.load(sys.stdin);print(d["crumbRequestField"]+":"+d["crumb"])')
post() { curl -s -b "$jar" -u "$auth" -H "$crumb" "$@"; }

echo "==> checking configured state"
post --data-urlencode "script@${HERE}/verify.groovy" "$URL/scriptText" | tee /tmp/verify.out
grep -q VERIFY_STATE_OK /tmp/verify.out || { echo "!! state check failed"; exit 1; }

echo "==> waiting for the auto-triggered master_seed"
for _ in $(seq 1 60); do
  result=$(curl -s -u "$auth" "$URL/job/master_seed/1/api/json" 2>/dev/null |
    python3 -c 'import sys,json;print(json.load(sys.stdin).get("result") or "")' 2>/dev/null || true)
  [ -n "${result:-}" ] && break
  sleep 2
done
if [ "${result:-}" != SUCCESS ]; then
  echo "!! master_seed finished ${result:-<no build>}"
  curl -s -u "$auth" "$URL/job/master_seed/1/consoleText" | tail -30
  exit 1
fi

echo "==> checking generated jobs"
generated=$(curl -s -u "$auth" "$URL/job/example/job/seed_job/api/json" |
  python3 -c 'import sys,json;print(json.load(sys.stdin)["fullName"])')
[ "$generated" = "example/seed_job" ] || { echo "!! seed job not generated"; exit 1; }

echo "PASS: bootstrap configured jenkins and master_seed generated ${generated}"
