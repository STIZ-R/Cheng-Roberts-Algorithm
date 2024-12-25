import akka.actor.ActorSystem;
import akka.actor.ActorRef;

public class Main {
    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create("Anneau");
        ActorRef anneau = system.actorOf(Anneau.props(20), "anneau");
        anneau.tell(new Anneau.Election(), ActorRef.noSender());


        //On évite que le terminate() se lance trop tôt
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        system.terminate();
    }
}
