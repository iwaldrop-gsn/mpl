import com.griddynamics.devops.mpl.Helper

def call(String name, Closure closure) {
	return call(name, Helper.configFromClosure(closure))
}

def call(String name, Map config = null) {
	def out = [:]
	stage (name) { out = MPLModule(name, config) }
	if (out.any()) echo "Module $name output merged to global config: ${MPLGlobalConfig()}"
	return out
}