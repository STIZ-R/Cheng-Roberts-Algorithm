import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe d'Acteur: Anneau
 * Elle permet de faire la représentation d'un groupe de Noeuds
 * Dans l'Algorithme de Chang Roberts, c'est la représentation du lien entre tous les noeuds.
 * <p>
 * Elle comporte les attributs suivants:
 * -noeuds: Liste d'Acteur Noeuds permettant de les liers entre-eux
 * -ids: Liste des ids pour chaque Acteur Noeud
 */
public class Anneau extends AbstractActor {
    private final List<ActorRef> noeuds = new ArrayList<>();
    private final List<Integer> ids = new ArrayList<>();

    /**
     * On fait une liste de nb nombres que l'on mélangera afin de donner une liste random.
     * ex: 12 -> [0;1;2;3;4;5;6;7;8;9;10;11]  =shuffle=> [10;7;4;3;8;6;11;2;9;5;0;1]
     * <p>
     * Ensuite, on créé les Acteurs Noeud avec comme identifiant celui dans la liste du dessus.
     * <p>
     * Enfin, on initialise le voisin de droite à chacun des noeuds.
     *
     * @param nb nombre de noeuds voulus dans l'anneau
     */
    private Anneau(int nb) {
        for (int i = 0; i < nb; i++) ids.add(i);
        Collections.shuffle(ids);

        for (int i = 0; i < ids.size(); i++) {
            ActorRef noeud = getContext().actorOf(Noeud.props(ids.get(i), getSelf()), "" + ids.get(i));
            this.noeuds.add(noeud);
            //Affichage de l'Anneau
            System.out.print("id " + ids.get(i) + (i != ids.size() - 1 ? " -> " : "\n"));
        }

        for (int i = 0; i < ids.size(); i++) {
            this.noeuds.get(i).tell(i < ids.size() - 1 ? new Noeud.SetVoisinDroite(this.noeuds.get(i + 1)) : new Noeud.SetVoisinDroite(this.noeuds.get(0)), getSelf());
        }

        printAnneau(ids);
    }


    /**
     * Représentation en Anneau dans le terminal.
     * Lien: https://codingface.com/print-a-circle-pattern-in-java/
     * https://btechgeeks.com/java-program-to-print-circle-number-pattern/
     * @param id anneau
     */
    private void printAnneau(List<Integer> id) {
        int size = id.size();
        if (size == 1) {
            System.out.println(" " + id.get(0));
        } else if (size == 2) {
            System.out.println(" " + id.get(0) + " <---> " + id.get(1));
        } else {
            int radius = (int) (0.5 * id.size());
            double angleStep = 2 * Math.PI / size;
            String[][] cercle = new String[2 * radius + 1][2 * radius + 1];

            for (int i = 0; i < cercle.length; i++) {
                for (int j = 0; j < cercle[i].length; j++) {
                    cercle[i][j] = "   ";
                }
            }

            for (int i = 0; i < size; i++) {
                int y = (int) Math.round(radius + radius * Math.cos(i * angleStep));
                int x = (int) Math.round(radius + radius * Math.sin(i * angleStep));
                cercle[x][y] = String.valueOf(id.get(i));
            }

            for (String[] row : cercle) {
                for (String ids : row) {
                    System.out.print(ids);
                }
                System.out.println();
            }
        }
        System.out.println();
    }



    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Election.class, msg -> election())
                .match(LeaderElu.class, msg -> informerLeader(msg.id))
                .build();
    }

    /**
     * Chaque noeud envoie à son voisin son id.
     * (Le dernier envoie au premier)
     */
    public void election() {
        for (int i = 0; i < noeuds.size(); i++) {
            ActorRef voisin = noeuds.get((i + 1) % noeuds.size());
            voisin.tell(new Noeud.Tour(ids.get(i)), getSelf());
        }
    }

    /**
     * Il y a un leader
     *
     * @param id renvoie l'id du leader
     */
    public void informerLeader(int id) {
        System.out.println("Le noeud " + id + " est élu Leader.");
    }

    public static Props props(int n) {
        return Props.create(Anneau.class, n);
    }

    /**
     * Interface Utils afin de ne pas mélanger les fonctions entre les Acteurs.
     */
    public interface Utils {
    }

    public static class Election implements Utils {
        public Election() {
        }

        ;
    }

    public static class LeaderElu implements Utils {
        public final int id;

        public LeaderElu(int id) {
            this.id = id;
        }

        ;
    }
}
