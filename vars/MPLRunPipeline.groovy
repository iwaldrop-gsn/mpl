def call(Closure closure) {
	try {
		closure.call()
	}
	catch (e) {
		echo "Pipeline failure: $e.message"
		currentBuild.result = 'FAILURE'
	}
	finally {
		MPLPostStepsRun()
	}
}
