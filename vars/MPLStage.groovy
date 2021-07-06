import com.griddynamics.devops.mpl.Helper

def call(String name, Closure closure) {
	return call(name, Helper.configFromClosure(closure))
}

def call(String name, Map config = null) {
	def out = [:]
	stage (name) { out = MPLModule(name, config) }
	return out
}