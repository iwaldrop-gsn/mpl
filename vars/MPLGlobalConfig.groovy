import com.griddynamics.devops.mpl.MPLManager

def call() { MPLManager.instance.globalConfig }

def propertyMissing(name) { MPLManager.instance.globalConfig[name] }