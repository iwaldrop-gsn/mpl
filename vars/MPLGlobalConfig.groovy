import com.griddynamics.devops.mpl.MPLManager

def call() { MPLManager.instance.globalConfig }

def propertyMissing(name) {
	echo "MPLGlobalConfig.propertyMissing: $name"
	MPLManager.instance.globalConfig[name]
}