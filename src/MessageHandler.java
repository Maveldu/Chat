import java.util.ArrayList;

/**	Classe définissant le module de traitement des messages
 * 	
 * 	Le module de traitement de message permet d'exécuter une commande et de constituer un message
 **/

public class MessageHandler {
	
	public MessageHandler(){}
	
	//Analyse une entrée pour savoir si il faut le traiter en tant que message ou commande
	protected void handle(Client client,String str){
		if(str.substring(0,1).equals("/")){
			//l'entrée est précédée par un '/', il faut la traiter comme une commande
    		this.cmdHandle(client,str.substring(1,str.length()));
		}else{
			//l'entrée est un message simple, il faut la traiter comme un message de groupe
			this.toMsg(client,null,str,"room");		
		}
	}
	
	//Constitue un message (String) et le transmet au module d'envoi
	private void toMsg(Client sender,Client receiver,String content,String type){
		String message;
		switch(type){
		//message de groupe
		case "room":
			message="("+sender.getRoom().getName()+")"+sender.getName()+"["+Chat.getTime()+"]>"+content;
			Chat.getSender().multicast(sender.getRoom(),message);
			break;
		//message global
		case "all":
			message="(a)"+sender.getName()+"["+Chat.getTime()+"]>"+content;
			Chat.getSender().broadcast(message);
			break;
		//message privé
		case "whisp":
			message="(w)"+sender.getName()+"->"+receiver.getName()+"["+Chat.getTime()+"]>"+content;
			Chat.getSender().whisper(sender,receiver,message);
			break;
		}
	}

	//Interprete et exécute une commande
	private void cmdHandle(Client client,String cmd){
		//séparation des arguments de la commande
		String[] args = null;
		args = cmd.split(" ");
		//gestion selon le nom de la commande (arg[0])
    	switch(args[0]){
    	//affiche les informations sur le serveur et les commandes possibles
    	case "?":
    		client.print(
    				"---------------------------------------------------\n" +
    				"Serveur de messagerie textuelle pour client léger\n" +
    				"Développé par Mavel DUSSARTRE\n" +
    				"---------------------------------------------------\n" +
    				"Commandes utilisateur :\n" +
    				"-------------\n" +
    				">[Message]\n" +
    				"Envoyer un message à toutes les personnes de votre salle de chat\n" +
    				"-------------\n" +
    				">/all [Message]\n" +
    				"Envoyer un message à toute les personnes connectées à ce serveur\n" +
    				"-------------\n" +
    				">/w [Destinataire] [Message]\n" +
    				"Envoyer un message privé\n" +
    				"-------------\n" +
    				">/re [Message]\n" +
    				"Répondre au dernier message privé reçu\n" +
    				"-------------\n" +
    				">/bye\n" +
    				"Se déconnecter\n" +
    				"-------------\n" +
    				">/goto [Salle de chat]\n" +
    				"Se déplacer dans une salle de chat\n" +
    				"-------------\n" +
    				">/listroom\n" +
    				"Lister les salles de chat\n" +
    				"-------------\n" +
    				">/listuser\n" +
    				"Lister les utilisateurs connectés\n" +
    				"-------------\n" +
    				">/lista\n" +
    				"Lister les messages globaux\n" +
    				"-------------\n" +
    				">/listr\n" +
    				"Lister les messages de la salle de chat\n" +
    				"-------------\n" +
    				">/listw\n" +
    				"Lister ses messages privés\n" +
    				"-------------\n" +
    				">/admin\n" +
    				"Passer en administrateur\n" +
    				"---------------------------------------------------\n"
    		);
    		if(client.isAdmin()){
    			client.print(
    					"Commandes administrateur :\n" +
    					"-------------\n" +	
    					">/shutdown\n" +
    					"Eteindre le serveur\n" +
    					"-------------\n" +	
    					">/kick [Utilisateur]\n" +
    					"Forcer un utilisateur à la déconnexion\n" +
        				"-------------\n" +
        				">/listr [Salle de chat]\n" +
        				"Lister les messages d'une salle de chat\n" +
        				"-------------\n" +
        				">/listw [Utilisateur]\n" +
        				"Lister les messages privés d'un utilisateur\n" +
    					"-------------\n" +	
    					">/moveto [Utilisateur] [Salle de chat]\n" +
    					"Déplacer un utilisateur dans une salle de chat\n" +
    					"-------------\n" +	
    					">/newroom [Nom]\n" +
    					"Créer une salle de chat\n" +
    					"-------------\n" +
    					">/delroom [Nom]\n" +
    					"Supprimer une salle de chat\n" +
    					"---------------------------------------------------\n"
    			);
    		}
    		client.br();
    		break;
    	//déconnecte le client
    	case "bye":
    		Chat.delClient(client);
    		break;
    	//passe l'utilisateur en administrateur
    	case "admin":
    		client.print("Mot de passe : ");
    		String pwd=client.scan();
	    	if(Chat.isPwd(pwd))
		    	client.setAdmin();
	    	else
	    		client.print("Mauvais mot de passe");client.br();
    		break;
    	//éteint le serveur
    	case "shutdown":
			if(client.isAdmin()){
				Chat.stop();
			}else{
				client.print("Vous devez être administrateur");client.br();
			}
			break;
		//force un utilisateur à la déconnexion
    	case "kick":
    		if(client.isAdmin()){
    			if(args.length==1){
    				client.print("Veuillez spécifier un nom d'utilisateur");client.br();
    			}else if(Chat.getClient(args[1])==null){
    				client.print("Veuillez spécifier un nom d'utilisateur valide");client.br();
    			}else{
    				Chat.delClient(Chat.getClient(args[1]));
    				client.print("Client "+args[1]+" supprimé");client.br();
    			}
    		}else{
    			client.print("Vous devez être administrateur");client.br();
    		}
    		break;
    	//déplace un utilisateur dans une salle de chat
    	case "moveto":	
    		if(client.isAdmin()){
    			if(args.length==1){
    				client.print("Veuillez spécifier un nom d'utilisateur");client.br();
    			}else if(Chat.getClient(args[1])==null){
    				client.print("Veuillez spécifier un nom d'utilisateur valide");client.br();
    			}else if(args.length==2){
    				client.print("Veuillez spécifier un nom de salle de chat");client.br();
    			}else if(Chat.getRoom(args[2])==null){
    				client.print("Veuillez spécifier un nom de salle de chat valide");client.br();
    			}else{
    				Chat.assignClient(Chat.getClient(args[1]), Chat.getRoom(args[2]));
    				client.print(args[1]+" a été déplacé dans "+args[2]);client.br();
    				Chat.getClient(args[1]).print("Vous avez été déplacé dans "+args[2]);client.br();
    			}
    		}else{
    			client.print("Vous devez être administrateur");client.br();
    		}
    		break;
    	//crée une nouvelle salle de chat
    	case "newroom":
    		if(client.isAdmin()){
    			if(args.length==1){
    				client.print("Veuillez spécifier un nom");client.br();
    			}else{
    				client.print(Chat.newRoom(args[1],Chat.getMaxMsg()));client.br();
    			}
    		}else{
    			client.print("Vous devez être administrateur");client.br();
    		}
    		break;
    	//supprime une salle de chat
    	case "delroom":
    		if(client.isAdmin()){
    			if(args.length==1){
     				client.print("Veuillez entrer un nom de salle de chat");client.br();
    			}else if(args[1].contentEquals(Chat.getDefRoom())){
    				client.print("Vous ne pouvez pas supprimer la salle de chat par défaut '"+Chat.getDefRoom()+"'");client.br();
    			}else if(Chat.getRoom(args[1])==null){
    				client.print("Veuillez entrer un nom de salle de chat valide");client.br();
    			}else{
    				Chat.delRoom(Chat.getRoom(args[1]));
    				client.print("Salle de chat '"+args[1]+"' supprimée");client.br();
    			}
    		}else{
    			client.print("Vous devez être administrateur");client.br();
    		}
    		break;
    	//liste les salles de chat
    	case "listroom":
			client.print("Liste des salles de chat :");client.br();
			ArrayList<ChatRoom> tmpRooms = new ArrayList<ChatRoom>(Chat.getRoomList());
			while(!tmpRooms.isEmpty()){
				client.print("- "+tmpRooms.remove(0).getName());client.br();
			}
    		break;
    	//liste les utilisateurs connectés
    	case "listuser":
			client.print("Liste des utilisateurs :");client.br();
			ArrayList<Client> tmpClients = new ArrayList<Client>(Chat.getClientList());
			while(!tmpClients.isEmpty()){
				client.print("- "+tmpClients.remove(0).getName());client.br();
			}
    		break;
    	//liste les messages de groupe
    	case "listr":
    		if(!client.isAdmin() || args.length==1){
    			client.print("Liste des messages de cette salle de chat :");client.br();
    			ArrayList<String> rtmpMsg = new ArrayList<String>(client.getRoom().getMsgList());
    			while(!rtmpMsg.isEmpty()){
    				client.print(rtmpMsg.remove(0));client.br();
    			}
    		}else{
    			client.print("Liste des messages de la salle de chat '"+args[1]+"' :");client.br();
    			ArrayList<String> rtmpMsg = new ArrayList<String>(Chat.getRoom(args[1]).getMsgList());
    			while(!rtmpMsg.isEmpty()){
    				client.print(rtmpMsg.remove(0));client.br();
    			}
    		}
    		break;
    	//liste les messages globaux
    	case "lista":
			client.print("Liste des messages globaux :");client.br();
			ArrayList<String> atmpMsg = new ArrayList<String>(Chat.getMsgList());
			while(!atmpMsg.isEmpty()){
				client.print(atmpMsg.remove(0));client.br();
			}
    		break;
    	//liste les messages privés
    	case "listw":
    		if(!client.isAdmin() || args.length==1){
    			client.print("Liste de vos messages privés :");client.br();
    			ArrayList<String> wtmpMsg = new ArrayList<String>(client.getMsgList());
    			while(!wtmpMsg.isEmpty()){
    				client.print(wtmpMsg.remove(0));client.br();
    			}
    		}else{
    			client.print("Liste des messages privés de '"+args[1]+"' :");client.br();
    			ArrayList<String> wtmpMsg = new ArrayList<String>(Chat.getClient(args[1]).getMsgList());
    			while(!wtmpMsg.isEmpty()){
    				client.print(wtmpMsg.remove(0));client.br();
    			}
    		}
    		break;
    	//déplace l'utilisateur actuel dans une salle de chat
    	case "goto" :
    		if(args.length==1){
    			client.print("Veuillez entrer un nom de salle de chat");client.br();
    		}else if(Chat.getRoom(args[1])==null){
    			client.print("Veuillez entrer un nom de salle de chat valide");client.br();
    		}else{
    			Chat.assignClient(client,Chat.getRoom(args[1]));
    			client.print("Vous vous êtes déplacés dans la salle "+args[1]);client.br();
    		}
    		break;
    	//envoie un message global
    	case "all":
    		if(args.length==1){
    			client.print("Veuillez entrer un message");client.br();
    		}else{
    			String msg = "";
    			for(int i=1;i<args.length;i++){
    				msg=msg+args[i]+" ";
    			}
    			this.toMsg(client,null,msg,"all");
    		}
    		break;
    	//envoie un message privé
    	case "w":
    		if(args.length==1){
    			client.print("Veuillez entrer un destinataire");client.br();
    		}else if(Chat.getClient(args[1])==null){
    			client.print("Veuillez entrer un destinataire valide");client.br();
    		}else if(args.length==2){
    			client.print("Veuillez entrer un message");client.br();
    		}else{
    			String msg = "";
    			for(int i=2;i<args.length;i++){
    				msg=msg+args[i]+" ";
    			}
    			this.toMsg(client,Chat.getClient(args[1]),msg,"whisp");
    		}
    		break;
    	//réponds à un message privé
    	case "re":
    		if(client.getLastWhisp()==null){
    			client.print("Personne ne vous a envoyé de message");client.br();
    		}else if(args.length==1){
    			client.print("Veuillez entrer un message");client.br();
    		}else{
    			String msg = "";
    			for(int i=1;i<args.length;i++){
    				msg=msg+args[i]+" ";
    			}
    			this.toMsg(client,client.getLastWhisp(),msg,"whisp");
    		}
    		break;
    	//commande non existante
    	default:
    		client.print("Cette commande n'existe pas");client.br();
    		break;
    	}
    }
}
