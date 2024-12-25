import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * Classe d'Acteur: Noeud
 *
 * Elle permet de faire la représentation des noeuds au sein d'un Anneau
 * pour l'Algorithme de Chang Roberts
 *
 * Elle comporte les attributs suivants:
 *      -id: l'id du noeud
 *      -voisinDroite: le voisin direct du noeud courant
 *      -passif: savoir si le noeud transmet le message ou non
 *      -leader: savoir si le noeud est leader (ne sert pas à grand chose actuellement)
 *
 */
public class Noeud extends AbstractActor {
    private final int id;
    private ActorRef voisinDroite;
    private boolean passif = false;
    private boolean leader = false;

    private Noeud(int id, ActorRef anneau) {
        this.id = id;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tour.class, msg -> tour(msg))
                .match(SetVoisinDroite.class, msg -> setVoisinDroite(msg))
                .build();
    }

    /**
     *  Si le noeud est déclaré passif alors il envoie au voisin de droite le message reçu directement.
     *  Sinon
     *      Si l'ID reçu est supérieur à son ID
     *          Alors le noeud actuel devient passif
     *      Sinon si l'ID reçu est inférieur à l'ID du noeud actuel
     *          Alors on le détruit
     *      Sinon si l'ID reçu est égal à l'ID du noeud actuel
     *          Alors on prévient l'Anneau qu'il y a un Leader élu (noeud actuel)
     *
     *
     *
     *              Définition du CM:
     *
     *  Fonctionne sur un anneau dirigé.
     *
     *  → lesnœuds d’un groupe d’initiateur envoient un message au
     *  nœud suivant (leur voisin) avec leur ID (𝑞)
     *
     *  quand un nœud 𝑝 actif reçoit un message avec le contenu 𝑞 trois
     *  cas sont définis :
     *  → si 𝑞 <𝑝 alors 𝑝 détruit le message
     *  → si 𝑞 >𝑝 alors 𝑝 devient passif et transmet le message
     *  → si 𝑞 =𝑝 alors 𝑝 devient le leader
     *
     *  Seul le message avec le plus grand id fera un tour comple
     *
     * @param msg Prend l'ID du noeud précédent afin de le comparer
     */
    public void tour(Tour msg) {
        if (this.passif) {
            System.out.println("Noeud " + this.id + " est déjà passif: " + msg.id);
            voisinDroite.forward(msg, getContext());
        } else {
            if (msg.id > this.id) {
                System.out.println("Noeud " + this.id + " devient passif: " + msg.id);
                this.passif = true;
                voisinDroite.forward(msg, getContext());
            } else if (msg.id < this.id) {
                System.out.println("Destruction du message: " + this.id + " > " + msg.id);
            } else if (msg.id == this.id) {
                System.out.println("Élection du leader: " + this.id);
                this.leader = true;
                getContext().parent().tell(new Anneau.LeaderElu(this.id), getSelf());

            }
        }
    }

    public void setVoisinDroite(SetVoisinDroite msg) {
        this.voisinDroite = msg.voisin;
    }

    public static Props props(int id, ActorRef anneau) {
        return Props.create(Noeud.class, id, anneau);
    }

    /**
     * Interface Utils afin de ne pas mélanger les fonctions entre les Acteurs.
     */
    public interface Utils {}

    public static class SetVoisinDroite implements Utils {
        public final ActorRef voisin;
        public SetVoisinDroite(ActorRef voisin) {
            this.voisin = voisin;
        };
    }

    public static class Tour implements Utils{
        public final int id;
        public Tour(int id) {
            this.id = id;
        };
    }
}
