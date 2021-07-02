import com.griddynamics.devops.mpl.MPLManager

def call() { MPLManager.instance.globalConfig }

def propertyMissing(name) { call()[name] }

//def propertyMissing(name, value) { call().(name) = value}