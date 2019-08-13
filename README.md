# Filtered Mail Plugin

## 概要
このプラグインは、システム設定で許可されたドメインにのみメールを送ることができるプラグインです。  
[Pipeline: Basic Steps](https://github.com/jenkinsci/workflow-basic-steps-plugin)の処理を参考に、フィルター機能を実装しています。  

## 使い方
### システム設定の追加
システム設定に`Filtered Mail Plugin`という項目があります。  
Filterという入力欄があるので、承認するドメインを入力してください(カンマ区切りで複数設定可能です)。  
ここで設定したドメインにのみメールを送信することができます。

### Pipeline
Pipeline Syntaxを使用すると簡単に設定することが可能です。  
`フィルター付きメール`を選択し、各種項目を入力後にPipelineで使用してください。

## 言語
日本語と英語に対応しています。  
Pipeline Syntaxのヘルプなどは、ブラウザの言語設定によって変わります。  
ジョブのコンソールは、Jenkinsインスタンスの言語設定によって変わります。
