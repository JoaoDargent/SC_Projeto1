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
    - alias: "serverKS"
    - key password for "serverKS": "serverpw"

Cliente:
    - truststore: "truststore.clients"
    - truststore password: "truststorepw"
    - alias: "clientsTS"

    - keystore: "keystore.<clientsName>"
    - keystore password: <client chooses>
    - alias: "<clientsName>KS"

Criar o par de chaves RSA do servidor e o seu certificado auto-assinado:
    keytool -genkeypair -alias serverKS -keyalg RSA -keysize 2048 -keystore keystore.server

Exportar o certificado auto-assinado do servidor:
    keytool -exportcert -alias serverKS -file certServer.cer -keystore keystore.server

Criar a truststore para a aplicação cliente com o certificado do servidor:
    keytool -import -alias clientsTS -file certServer.cer -keystore truststore.clients

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