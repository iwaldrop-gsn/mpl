def call(Closure closure) {
	try {
		closure.call()
		currentBuild.result = 'SUCCESS'
	}
	catch (e) {
		echo "Pipeline failure: $e.message"
		currentBuild.result = 'FAILURE'
	}
	finally {
		MPLPostStepsRun()
	}
}
