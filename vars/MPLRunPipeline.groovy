def call(Closure closure) {
	try {
		closure.call()
		currentBuild.result = 'SUCCESS'
	}
	catch (InterruptedException e) {
		echo "Pipeline aborted: $e.message"
		currentBuild.result = 'ABORTED'
	}
	catch (e) {
		echo "Pipeline failure: $e.message"
		currentBuild.result = 'FAILURE'
	}
	finally {
		MPLPostStepsRun()
	}
}
