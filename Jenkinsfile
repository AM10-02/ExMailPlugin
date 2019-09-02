node('master') {
	// currentBuild.resultだとsandboxに引っかかるので別途定義
	def result = null

	try {
		stage('setup') {
			// プラグインをGitHubリポジトリから取得します
			checkout poll: false, scm: [
				$class: 'GitSCM', branches: [[name: env.BRANCH_NAME]],
				doGenerateSubmoduleConfigurations: false,
				extensions: [
					[$class: 'CheckoutOption', timeout: 60],
					[$class: 'CloneOption', depth: 0, noTags: false, reference: '', shallow: false, timeout: 60],
					[$class: 'CleanBeforeCheckout'],
					[$class: 'PruneStaleBranch']
				],
				submoduleCfg: [],
				userRemoteConfigs: [[url: 'https://github.com/AM10-02/FilteredMailPlugin.git']]
			]
		}

		stage('install') {
			lock(label: 'filtered-email-plugin') {
				sh "mvn dependency:copy-dependencies"
			}
		}

		stage('clean') {
			// mvnのcleanを行います
			sh "mvn clean"
		}

		stage('package') {
			// パッケージを作成します
			// ビルドパラメータによって、テストを行うかのオプションを変更します
			sh "mvn package -DskipTests=true"
		}

		stage('archive') {
			// 作成したプラグインを成果物として保存します
			archiveArtifacts 'target/*.hpi'
		}
	} catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
		// 中断したときはresultをabortedに設定します
		currentBuild.rawBuild.result = Result.ABORTED
		throw e
	} catch (Exception e) {
		// エラーが発生したときはresultをfailureに設定します
		currentBuild.rawBuild.result = Result.FAILURE
		throw e
	} finally {
		// resultに合わせてメールを送信します
		def message = ""
		switch (Result.fromString(currentBuild.currentResult)) {
			case Result.SUCCESS:
				message = """${env.BUILD_URL}
ビルドが成功しました
"""
				break
			case Result.UNSTABLE:
				message = """${env.BUILD_URL}
ビルドは成功しましたが、警告が発生しています
"""
			case Result.ABORTED:
				message = """${env.BUILD_URL}
ビルドが中断されました
"""
				break
			case Result.FAILURE:
				message = """${env.BUILD_URL}
ビルドが失敗しました
"""
				break
		}
		mail body: message, from: 'akito_akegarasu@hotmail.co.jp', subject: 'ビルド完了通知', to: 'jenkins-sample@exsample.co.jp'
	}
}
