// Buddy Server Program
// Patrick Gephart

#include<stdio.h>
#include<string.h> //strlen
#include<sys/socket.h>
#include<arpa/inet.h> //inet_addr
#include<unistd.h> //write
#include<errno.h> //errors

/*
 //TRY TO RECIEVE AND TEST [DEL ME]
				datarecvA = recv(clientAread, clientAbuffer, 6000, 0);
					
					if(datarecvA)
					{
					printf("A: %s (%d)\n", clientAbuffer, datarecvA);
					errsv = errno;
					}
					
					printf("A, After B Socket Creation: %d\n", errsv);
*/	

int main(int argc , char *argv[])
{
	const int runforever = 0;

	int resetA = 0;
	int resetB = 0;
	
	int clientAconnect = 0;
	int clientBconnect = 0;
		
	int clientAsocket = 0;
	int clientBsocket = 0;
		
	int cAsize = 0;
	int cBsize = 0;
	
	struct sockaddr_in clientA = {0};
	struct sockaddr_in clientB = {0};
			
	//DATA BUFFERS
	unsigned char clientAbuffer[12000];
	unsigned char clientBbuffer[12000];
	
	int clientAread = 0;
	int clientBread = 0;
		
	int aHANGUP = 0;
	int bHANGUP = 0;
		
	
	//THIS DO LOOP KEEPS THE PROGRAM RUNNING FOREVER.
	do
	{
		//CONSTANTS
		const char nu = NULL;
		const char change[19] = "CHANGE ACCEPTED.";
		const char nouser[30] = "ERROR 1: NO SUCH USER EXISTS.";
		const char invald[26] = "ERROR 2: INVALID COMMAND.";
		const char shut[2] = "!";

		//SERVER REQUIRED OBJECTS
		int socket_desc = 0;
		int client_sock = 0;
		int c = 0;
		int read_size = 0;
		
		struct sockaddr_in server = {0};
		struct sockaddr_in client = {0};
		
		
		//CAST STRINGS FOR COMMANDS
		char userID[3] = "XXX";
		char userIP[16] = "YYY.YYY.YYY.YYY";
		char fileloc[23] = "/home/pi/Users/XXX.txt";
		char tarfile[23] = "/home/pi/Users/XXX.txt";
		char target[20] = "XXX/YYY.YYY.YYY.YYY";
		char tarID[4];
		
		//FILE TOOLS
		FILE *writefile = NULL;
		FILE *readfile = NULL;
	
		//VARIABLES
		char client_message[2000];
		int n = 0;
		int counter = 0;
		
		int endedfile = 0;
		char curr = 'a';
		int optval = 1;
	
		int sendforever = 0;

		int countup = 0;
		int resetcounter = 0;
		
		
		int aEMPTY = 0;
		int bEMPTY = 0;

		int datarecvA = 0;
		int datarecvB = 0;
		int errsv = 0;
		int senderrA = 0;
		int senderrB = 0;

		if(resetA)
		{
			//TRY TO RECIEVE AND TEST [DEL ME]
				datarecvA = recv(clientAread, clientAbuffer, 6000, 0);
					
					if(datarecvA)
					{
					printf("A: %s (%d)\n", clientAbuffer, datarecvA);
					errsv = errno;
					}
					
					printf("A, Start of New Loop: %d\n", errsv);
				}

		//CREATE THE SOCKET
		socket_desc = socket(AF_INET , SOCK_STREAM , 0);
		if (socket_desc == -1)
		{
			printf("Could not create socket");
			return -1;
		}
		puts("Socket created");
			
		//PREPARE THE SOCKADDR_IN <SOCKET> STRUCTURE
		server.sin_family = AF_INET;
		server.sin_addr.s_addr = INADDR_ANY;
		server.sin_port = htons(1199);
		setsockopt(socket_desc, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof optval);

		//BIND
		if( bind(socket_desc,(struct sockaddr *)&server , sizeof(server)) < 0)
		{
			//BIND ERROR MESSAGE
			perror("bind failed. Error");
			return -2;
		}
		puts("bind done");

		//LISTEN AT SOCKET
		listen(socket_desc , 3);
		puts("Waiting for incoming connections...");

		//ACCEPT AN INCOMING CONNECTION
		c = sizeof(struct sockaddr_in);

		//ACCEPT THE CONNECTION FROM THE INCOMING CLIENT
		client_sock = accept(socket_desc, (struct sockaddr *)&client, (socklen_t*)&c);

		//CHECKS ACCEPTANCE
		if (client_sock < 0)
		{
			perror("accept failed");
		}
		puts("Connection accepted.");
		
		//RECIEVE A MESSAGE FROM THE CLIENT
		while( (read_size = recv(client_sock , client_message , 2000 , 0)) > 0 )
		{

			//CHECK THE COMMAND (first digit of client message) AND OPERATE

			//OPERATION 1 (client IP update command)
			if(client_message[0] == '1')
			{
				printf("UPDATING: User ");
				
				//GET THE USER ID
				for(n=2; client_message[n] != '/'; n++)
				{
					userID[counter] = client_message[n];
					counter++;
				}
				printf(userID);
				printf(" to IP address ");
				
				//RESET COUNTER
				counter = 0;
				
				//PASSES OVER THE '/' AFTER THE USER ID
				n++;
				
				//GETS THE USER IP ADDRESS
				for(n=n; ((client_message[n] >= '0' && client_message[n] <= '9') || client_message[n] == '.'); n++)
				{
					userIP[counter] = client_message[n];
					counter++;
				}
				
				//REPLACES ANY NON-IP CHARACTER IN THE USER IP STRING WITH NULLS
				for(counter=counter; counter != 15; counter++)
				{
					userIP[counter] = nu;
				}
				printf(userIP);
				
				//SETS THE FILE LOCATION STRING WITH THE USER ID STRING
				fileloc[15] = userID[0];
				fileloc[16] = userID[1];
				fileloc[17] = userID[2];
				
				//PRINTS THE FILE LOCATION
				printf("\nFILE LOCATION: ");
				printf(fileloc);
				
				//WRITES THE IP ADDRESS STRING TO THE FILE LOCATION
				writefile = fopen(fileloc, "w+");
				fprintf(writefile, "%s", userIP);
				fclose(writefile);
				
				//SEND THE ACKNOWLEDGE MESSAGE TO THE USER
				send(client_sock, change, (int)strlen(change)+1, 0);
			}

			//OPERATION 2 (friend IP request from client)
			else if(client_message[0] == '2')
			{

				//GETS THE FRIEND'S USER ID
				tarID[0] = client_message[2];
				tarID[1] = client_message[3];
				tarID[2] = client_message[4];
				
				//SETS THE FILE TARGET STRING WITH THE USER'S ID
				tarfile[15] = tarID[0];
				tarfile[16] = tarID[1];
				tarfile[17] = tarID[2];
				
				//STATES ACTION TO SERVER SCREEN
				printf("SENDING: User ");
				printf(tarID);
				printf(" from file ");
				printf(tarfile);
				
				//SETS TARGET STRING FOR SENDING TO CLIENT LATER
				target[0] = tarID[0];
				target[1] = tarID[1];
				target[2] = tarID[2];
				
				//TRIES TO READ THE FILE CONNECTED TO THE FRIEND USER ID
				readfile = fopen(tarfile, "r");
				
				//IF THE FILE DOESN'T EXIST, SEND AN ERROR TO THE CLIENT
				if(readfile == NULL)
				{
					printf("\nFILE DOES NOT EXIST.");
					send(client_sock, nouser, ((int)strlen(nouser)+1), 0);
				}
				
				//IF THE FILE EXISTS, SEND THE FRIEND'S IP
				else
				{
					//READ THE IP ADDRESS FROM THE FILE INTO THE TARGET STRING
					for(n=4; endedfile != EOF; n++)
					{
						endedfile = fscanf(readfile, "%c", &curr);
						target[n] = curr;
					}
					
					//EMPTY THE REST OF THE STRING WITH NULLS
					for(n--; n<=(int)strlen(target); n++)
					{
					target[n] = nu;
					}
					
				//SEND THE TARGET STRING TO THE CLIENT
				send(client_sock, target, ((int)strlen(target)+1), 0);
				}
			}
			
			//OPERATION 5 (client requests a port)
			else if(client_message[0] == '5')
			{
				//SET PORT VARIABLE TO CLIENT A
				if(clientAconnect == 0)
				{
					clientAconnect = 1;
					send(client_sock, "1201", 5, 0);
				}
				
				//SET PORT VARIABLE TO CLIENT B
				else if(clientBconnect == 0)
				{
					clientBconnect = 1;
					send(client_sock, "1205", 5, 0);
				}
				
				//REFUSE A PORT ASSIGNMENT TO THIRD CLIENT
				else
				{
					printf("SOCKETS ARE BOTH OCCUPIED.");
				}
			}

			//OPERATION 6 (flushes sockets)
			else if(client_message[0] == '6')
			{
				send(client_sock, "Flushing System.\n", 35, 0);

				close(socket_desc);
				close(client_sock);
				close(clientAread);
				close(clientBread);
				close(clientAsocket);
				close(clientBsocket);

				resetA = 0;
				resetB = 0;
				clientAconnect = 0;
				clientBconnect = 0;
				clientAsocket = 0;
				clientBsocket = 0;
				cAsize = 0;
				cBsize = 0;
				socket_desc = 0;
				client_sock = 0;
				c = 0;
				read_size = 0;
				clientAbuffer[0] = '\0';
				clientBbuffer[0] = '\0';
				client_message[0] = '\0';
				n = 0;
				counter = 0;
				endedfile = 0;
				sendforever = 0;
				countup = 0;
				resetcounter = 0;
				clientAread = 0;
				clientBread = 0;
				aHANGUP = 0;
				bHANGUP = 0;
				aEMPTY = 0;
				bEMPTY = 0;

			}

			//SEND AN ERROR TO THE CLIENT IF THE OPERATION DOES NOT EXIST
			else
			{
				printf("ERROR - COMMAND IS NOT VALID");
				send(client_sock, invald, ((int)strlen(invald)+1), 0);
			}
		}

		//SEND THE SHUT CHARACTER TO THE CLIENT TO TELL THEM THE CONNECTION IS OVER
		send(client_sock, shut, ((int)strlen(shut)+1), 0);

		//FLUSH STDIN\OUT AND REPORT WHEN CLIENT STOPS SENDING DATA
		if(read_size == 0)
		{
			puts("\nClient disconnected");
			fflush(stdout);
			fflush(stdin);
		}
		
		//IF THE READ SIZE FAILS, DISPLAY MESSAGE TO SERVER
		else if(read_size == -1)
		{
			perror("recv failed");
		}
		
		//CLOSE THE SOCKET
		close(socket_desc);
		
		//READY SOCKET FOR CLIENT A (with error checking)
		if(clientAconnect == 1 && resetA == 0)
		{
			clientAsocket = socket(AF_INET, SOCK_STREAM, 0);
			
			if (clientAsocket == -1)
			{
				printf("Could not create client A bridge socket");
				return -1;
			}
			puts("Client A Bridge Socket Created");
			
			//CREATE SOCKET A'S SOCKADDR_IN STRUCTURE
			clientA.sin_family = AF_INET;
			clientA.sin_addr.s_addr = INADDR_ANY;
			clientA.sin_port = htons(1201);
			setsockopt(clientAsocket, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof optval);
			puts("Client A SockAddr_IN Structure Created and Set");
			
			//BIND SOCKET A
			if(bind(clientAsocket,(struct sockaddr *)&clientA, sizeof(clientA)) < 0)
			{
				//BIND SOCKET A ERROR MESSAGE
				perror("Client A Bind Failed.");
				return -4;
			}
			puts("Client A Binding Done");
			
			//LISTEN AT CLIENT A SOCKET FOR CONNECTION
			listen(clientAsocket, 0);
			puts("Waiting for Client A Connection...");

			//ACCEPT CLIENT A INCOMING CONNECTION
			cAsize = sizeof(struct sockaddr_in);
			clientAread = accept(clientAsocket, (struct sockaddr *)&clientA, (socklen_t*)&cAsize);

			//CHECKS CLIENT A ACCEPTANCE
			if (clientAsocket < 0)
			{
				perror("Client A Acceptance Failed");
			}
			puts("Connection A Accepted.");
		
			//SETS RESET SO THE SOCKET ISN'T OPENED OVER AND OVER
			resetA = 1;	
		}
	
		//READY SOCKET FOR CLIENT B (with error checking)
		if(clientBconnect == 1 && resetB == 0)
		{
			clientBsocket = socket(AF_INET, SOCK_STREAM, 0);
		
		
			if (clientBsocket == -1)
			{
				printf("Could not create client B bridge socket");
				return -1;
			}
			else if (clientBsocket == 0)
			{
				printf("Client B Socket Zero");
			}
			else
			{
				puts("Client B Bridge Socket Created");
			}
			
			//CREATE SOCKET B'S SOCKADDR_IN STRUCTURE
			clientB.sin_family = AF_INET;
			clientB.sin_addr.s_addr = INADDR_ANY;
			clientB.sin_port = htons(1205);
			setsockopt(clientBsocket, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof optval);
			puts("Client B SockAddr_IN Structure Created and Set");
				
			//BIND SOCKET B
			if(bind(clientBsocket,(struct sockaddr *)&clientB, sizeof(clientB)) < 0)
			{
				//BIND SOCKET B ERROR MESSAGE
				perror("Client B Bind Failed.");
				return -4;
			}
			puts("Client B Binding Done");
			
			//LISTEN AT CLIENT B SOCKET FOR CONNECTION
			listen(clientBsocket, 0);
			puts("Waiting for Client B Connection...");

			//ACCEPT CLIENT B INCOMING CONNECTION
			cBsize = sizeof(struct sockaddr_in);
			clientBread = accept(clientBsocket, (struct sockaddr *)&clientB, (socklen_t*)&cBsize);
			
			//CHECKS CLIENT B ACCEPTANCE
			if (clientBsocket < 0)
			{
				perror("Client B Acceptance Failed");
			}
			puts("Connection B Accepted.");
			
			//SETS RESET SO THE SOCKET ISN'T OPENED OVER AND OVER
			resetB = 1;
			
		}
		
		aHANGUP = 0;
		bHANGUP = 0;
		
		if(clientAsocket != 0 && clientBsocket != 0)
		{
			do
			{
				if(aHANGUP == 0 && bHANGUP == 0)
				{
					datarecvA = recv(clientAread, clientAbuffer, 6000, 0);
					datarecvB = recv(clientBread, clientBbuffer, 6000, 0);
					
					senderrA = send(clientAsocket, clientBbuffer, 6001, 0);
					
					if(senderrA)
					{
					printf("Send Err A: %s (%d)\n", clientAbuffer, senderrA);
					errsv = errno;
					printf("%d\n", errsv);
					return 2;
					}
					
					senderrB = send(clientBsocket, clientAbuffer, 6001, 0);
					
					if(senderrB)
					{
					printf("B: %s (%d)\n", clientBbuffer, senderrB);
					errsv = errno;
					printf("%d\n", errsv);
					return 3;
					}
				}
					
				for(counter = 0; counter < sizeof(clientAbuffer); counter++)
				{
					clientAbuffer[counter] = '\0';
					clientBbuffer[counter] = '\0';
				}
			}while(1);
		}
	}while(runforever == 0);

//END PROGRAM (we'll never reach here if everything goes correctly)
return 0;
}
