import com.griddynamics.devops.mpl.MPLManager

def call() { MPLManager.instance.globalConfig }

def get(name) { MPLManager.instance.globalConfig.get(name) }

//def propertyMissing(name) { MPLManager.instance.globalConfig[name] }