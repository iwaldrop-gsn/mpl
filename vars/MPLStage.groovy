import com.griddynamics.devops.mpl.Helper

def call(String name, Closure closure) {
	call(name, Helper.configFromClosure(closure))
}

def call(String name, Map config = null) {
	stage (name) { MPLModule(name, config) }
}