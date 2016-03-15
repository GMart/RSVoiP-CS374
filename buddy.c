// Buddy Server Program

#include<stdio.h>
#include<string.h> //strlen
#include<sys/socket.h>
#include<arpa/inet.h> //inet_addr
#include<unistd.h> //write

int main(int argc , char *argv[])
{
int runforever = 0;
do
{
int socket_desc , client_sock , c , read_size = 0;
struct sockaddr_in server , client = {0};
char client_message[2000];
const char change[19] = "CHANGE ACCEPTED.";
const char nouser[30] = "ERROR 1: NO SUCH USER EXISTS.";
const char invald[26] = "ERROR 2: INVALID COMMAND.";
const char shut[2] = "!";

char userID[3] = "XXX";
int n = 0;
int counter = 0;
char userIP[16] = "YYY.YYY.YYY.YYY";
char fileloc[23] = "/home/pi/Users/XXX.txt";
char tarfile[23] = "/home/pi/Users/XXX.txt";
char tarID[4];
char target[20] = "XXX/YYY.YYY.YYY.YYY";
FILE *writefile = NULL;
FILE *readfile = NULL;
const char nu = NULL;
int endedfile = 0;
char curr = 'a';
int optval = 1;

//Create socket
socket_desc = socket(AF_INET , SOCK_STREAM , 0);
if (socket_desc == -1)
{
printf("Could not create socket");
}
puts("Socket created");
//Prepare the sockaddr_in structure
server.sin_family = AF_INET;
server.sin_addr.s_addr = INADDR_ANY;
server.sin_port = htons(1199);
setsockopt(socket_desc, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof optval);
//Bind
if( bind(socket_desc,(struct sockaddr *)&server , sizeof(server)) < 0)
{
//print the error message
perror("bind failed. Error");
return 1;
}
puts("bind done");
//Listen
listen(socket_desc , 3);
//Accept and incoming connection
puts("Waiting for incoming connections...");
c = sizeof(struct sockaddr_in);
//accept connection from an incoming client
client_sock = accept(socket_desc, (struct sockaddr *)&client, (socklen_t*)&c);
if (client_sock < 0)
{
perror("accept failed");
}
puts("Connection accepted.");
//Receive a message from client
while( (read_size = recv(client_sock , client_message , 2000 , 0)) > 0 )
{
//Send the message back to client
//write(client_sock , client_message , strlen(client_message));
if(client_message[0] == '1')
{
printf("UPDATING: User ");
for(n=2; client_message[n] != '/'; n++)
{
userID[counter] = client_message[n];
counter++;
}
printf(userID);
printf(" to IP address ");
counter = 0;
n++;
for(n=n; ((client_message[n] >= '0' && client_message[n] <= '9') ||
client_message[n] == '.'); n++)
{
userIP[counter] = client_message[n];
counter++;
}
for(counter=counter; counter != 15; counter++)
{
userIP[counter] = nu;

}
printf(userIP);
fileloc[15] = userID[0];
fileloc[16] = userID[1];
fileloc[17] = userID[2];
printf("\nFILE LOCATION: ");
printf(fileloc);
writefile = fopen(fileloc, "w+");
fprintf(writefile, "%s", userIP);
fclose(writefile);
send(client_sock, change, (int)strlen(change)+1, 0);
}
else if(client_message[0] == '2')
{
tarID[0] = client_message[2];
tarID[1] = client_message[3];
tarID[2] = client_message[4];
tarfile[15] = tarID[0];
tarfile[16] = tarID[1];
tarfile[17] = tarID[2];
printf("SENDING: User ");
printf(tarID);
printf(" from file ");
printf(tarfile);
target[0] = tarID[0];
target[1] = tarID[1];
target[2] = tarID[2];
readfile = fopen(tarfile, "r");
if(readfile == NULL)
{
printf("\nFILE DOES NOT EXIST.");
send(client_sock, nouser, ((int)strlen(nouser)+1), 0);
}
else
{
for(n=4; endedfile != EOF; n++)
{
endedfile = fscanf(readfile, "%c", &curr);
target[n] = curr;
}
for(n--; n<=(int)strlen(target); n++)
{
target[n] = nu;
}
send(client_sock, target, ((int)strlen(target)+1), 0);
}
}
else
{
printf("ERROR - COMMAND IS NOT VALID");
send(client_sock, invald, ((int)strlen(invald)+1), 0);
}
}

send(client_sock, shut, ((int)strlen(shut)+1), 0);

if(read_size == 0)
{
puts("\nClient disconnected");
fflush(stdout);
fflush(stdin);
}
else if(read_size == -1)
{
perror("recv failed");
}
close(socket_desc);
}while(runforever == 0);
return 0;

}
