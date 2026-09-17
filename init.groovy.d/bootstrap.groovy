/*
 * The only script Jenkins runs at startup. Everything it does lives in
 * /usr/share/jenkins/config, which is read straight from the image, so the
 * logic stays current even when this file is a stale copy in jenkins_home.
 */
def loader = new GroovyClassLoader(this.class.classLoader)
loader.addClasspath('/usr/share/jenkins/config')
loader.loadClass('com.jenkins.config.Bootstrap').newInstance().run()
