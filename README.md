# Projeto1SC
Projeto 1 de SC de 22/23

## Para correr o programa
Inicia-se os jar's na pasta destes com o seguinte comando:
java -jar <nome do jar>

As fotos que pretendam ser enviadas têm que estar dentro da pasta chamada projeto


Primeiro tem que se iniciar o servidor com os parâmetros pedidos no enunciado:
*TintolmarketServer port*

Depois inicia-se o cliente:
*Tintolmarket serverAddress userID password*

Sendo que serverAddress tem o seguinte tipo: <IP/hostname>[:Port]

## Keystores e truststore
Servidor:
    - keystore: "keystore.server"
    - keystore password: "serverpw"
    - keypair: "serverKS"
    - keypair password for "serverKS": "serverpw"

Cliente:
    - truststore: "truststore.clients"
    - truststore password: "truststorepw"
    - alias: "clientsTS"

    - keystore: "keystore.filipa"
    - keystore password: filipapw
    - alias: "filipaKS"

    - keystore: "keystore.joao"
    - keystore password: joaopw
    - alias: "joaoKS"

Criar o par de chaves RSA do servidor e o seu certificado auto-assinado:
    keytool -genkeypair -alias serverKS -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.server

Exportar o certificado auto-assinado do servidor:
    keytool -exportcert -alias serverKS -file certServer.cer -storetype PKCS12 -keystore keystore.server

Criar a truststore para a aplicação cliente com o certificado do servidor:
    keytool -import -alias serverKS -file certServer.cer -storetype JKS -keystore truststore.clients


Criar o par de chaves RSA do cliente <filipa> e o seu certificado auto-assinado:
    keytool -genkeypair -alias filipaKS -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.filipa

Exportar o certificado auto-assinado do cliente <filipa>:
    keytool -exportcert -alias filipaKS -file certfilipa.cer -storetype PKCS12 -keystore keystore.filipa

Importar certificado do cliente <filipa> para a truststore:
    keytool -importcert -alias filipaKS -file certfilipa.cer -storetype JKS -keystore truststore.clients

Criar o par de chaves RSA do cliente <joao> e o seu certificado auto-assinado:
    keytool -genkeypair -alias joaoKS -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.joao

Exportar o certificado auto-assinado do cliente <joao>:
    keytool -exportcert -alias joaoKS -file certjoao.cer -storetype PKCS12 -keystore keystore.joao

Importar certificado do cliente <joao> para a truststore:
    keytool -importcert -alias joaoKS -file certjoao.cer -storetype JKS -keystore truststore.clients

Ver o conteúdo da keystore do servidor:
    keytool -list -storetype JCEKS -keystore keystore.server

Apagar entradas da truststore:
    keytool -delete -alias filipaKS -keystore truststore.clients

## Funcionalidades atuais

Programa cria user e regista-o. Caso o servidor seja finalizado faz "reload" dos users, ou seja:

O user "a" com a password "a" pode fazer login novamente e manter o seu balance anterior.

Para os vinhos, estes são criados.
É possível:
Add
Sell
View
Buy
Wallet
Classify
Talk
Read

## Limitações

Os vinhos e saldo não estão a ser "reloaded" após reinicio do servidor