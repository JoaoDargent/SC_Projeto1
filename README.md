# Projeto1SC
Projeto 1 de SC de 22/23


## Pré-requisitos

Ter java instalado na máquina


## Para correr o programa
Primeiro tem que se iniciar o servidor com os parâmetros pedidos no enunciado:
*TintolmarketServer port*

Depois inicia-se o cliente:
*Tintolmarket serverAddress userID password*

Sendo que serverAddress tem o seguinte tipo: <IP/hostname>[:Port]

Password não obrigatória neste passo

## Funcionalidades atuais

Programa cria user e regista-o mas caso o servidor seja finalizado não faz "reload" dos users, ou seja:

O user "a" com a password "a" pode finalizar o client e reiniciar que continua a ser identificado mas caso o servidor seja reiniciado este terá que se registar novamente

Funcionalidade wallet implementada

## Limitações

O comando "view" não funciona na totalidade, sendo que a imagem não é transferida corretamente.
