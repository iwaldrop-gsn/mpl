import com.griddynamics.devops.mpl.MPLManager

def call() { MPLManager.instance.globalConfig }

def propertyMissing(String name) {
	echo "MPLGlobalConfig.propertyMissing: $name"
	MPLManager.instance.globalConfig[name]
}