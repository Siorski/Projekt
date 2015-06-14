package projekt.agents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jadeOWL.base.OntologyManager;
import jadeOWL.base.messaging.ACLOWLMessage;

public class AgentTrip extends Agent{

	private static final long serialVersionUID = 5862645069068601092L;
	OntologyManager ontologyManager;
	OWLOntology holidayOntology;

	@Override
	protected void setup(){
		System.out.println("Agent " + getLocalName() + " started");

		ontologyManager = new OntologyManager();
		try {
			holidayOntology = ontologyManager.loadAndMapOntology(new File("ontologies/holidayOntology.owl"), "http://semantykaProjekt.com/holidayOntology.owl");
			//Listen for client messages
			addBehaviour(new RecieveClientMessages(this));

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void takeDown(){
		//Czyszczenie
		ontologyManager.removeAndUnmapAllOntologies();
	}
	protected String stripFromIRI(String s) {
		String[] temp = s.split("#");
		return temp[1].split(">")[0];
	}
	class RecieveClientMessages extends CyclicBehaviour{

		private static final long serialVersionUID = 7569347209298378146L;
		public OWLOntology ontology;
		public RecieveClientMessages(Agent a){
			super(a);
		}
		public boolean isInteger(String s) {
		    try { 
		        Integer.parseInt(s); 
		    } catch(NumberFormatException e) { 
		        return false; 
		    }
		    return true;
		}
		@Override
		public void action() 
		{

			System.out.println("Agent " + getLocalName() + " listening for client messages");
			ACLOWLMessage msg= (ACLOWLMessage) blockingReceive();
			System.out.println("Agent " + getLocalName() + " received message");
			if (msg!=null) {
				try {
					ontology = msg.getContentOntology(ontologyManager,myAgent);
					if(msg.getConversationId() == "Preferences") {
						Set<OWLClass> filteredSet = ontologyManager.getQueryManager().filterOWLQueryClasses(ontology);
						if(!filteredSet.isEmpty()) {
							ontologyManager.addImportToOntology(ontology, holidayOntology);
							Set<OWLNamedIndividual> individuals = ontologyManager.getQueryManager().getInstancesForClassQuery(filteredSet.iterator().next(), ontology);
							ACLOWLMessage msgAnswer = new ACLOWLMessage(ACLMessage.QUERY_IF);
							OWLOntology answerOnto = ontologyManager.getQueryManager().prepareQueryAnswerFromInstances(individuals, holidayOntology, myAgent);
							msgAnswer.addReceiver(new AID("client", AID.ISLOCALNAME));
							try {
								msgAnswer.setOntology(msg.getOntology());
								msgAnswer.setContentOntology(answerOnto);
								System.out.println("Agent " + getLocalName() + " answering Client");
								msgAnswer.setConversationId(msg.getConversationId());
								send(msgAnswer);
							} catch (OWLOntologyStorageException e) {
								e.printStackTrace();
							}
						}
					}
					else {
						Set<OWLNamedIndividual> indReturn = new HashSet<OWLNamedIndividual>();

						ArrayList<Set<OWLNamedIndividual>> indTemp = new ArrayList<Set<OWLNamedIndividual>>();
						indReturn.clear();
						indTemp.clear();
						// Pobieramy individuale (daty)
						Set<OWLNamedIndividual> individuals = ontologyManager.getQueryManager().filterAnswerSetInstances(ontology);
						OWLClass c = ontologyManager.getDataFactory().getOWLClass(holidayOntology, "Date");
						Set<OWLNamedIndividual> dateInd = ontologyManager.getQueryManager().getInstancesForClassQuery(c, holidayOntology);
						Iterator<OWLNamedIndividual> dateIterator = dateInd.iterator();
						// Lecimy po wszystkich datach
						while(dateIterator.hasNext()) {
							// Ile preferencji siê zgadza
							int howMany = 0;
							Set<OWLNamedIndividual> datePreferences = new HashSet<OWLNamedIndividual>();
							Set<OWLNamedIndividual> durationPreferences = new HashSet<OWLNamedIndividual>();
							Set<OWLNamedIndividual> transportPreferences = new HashSet<OWLNamedIndividual>();
							// Nasza data
							OWLNamedIndividual date = dateIterator.next();
							// Pobieramy object properties z daty
							Map<OWLObjectPropertyExpression, Set<OWLIndividual>> dateProperties = date.getObjectPropertyValues(holidayOntology);
							// Pobieramy klucze
							Iterator<OWLObjectPropertyExpression> dateKeys = dateProperties.keySet().iterator();
							// Lecimy po kluczach
							while(dateKeys.hasNext()) {
								// klucz
								OWLObjectPropertyExpression dateKey = dateKeys.next();
								// szukamy preferencji
								if(stripFromIRI(dateKey.toString()).equals("hasPreference")) {
									Iterator<OWLIndividual> valIterator = dateProperties.get(dateKey).iterator();
									while(valIterator.hasNext()) {
										OWLNamedIndividual preference = (OWLNamedIndividual) valIterator.next();
										datePreferences.add(preference);
									}	
								}
								if(stripFromIRI(dateKey.toString()).equals("hasDuration")) {
									Iterator<OWLIndividual> valIterator = dateProperties.get(dateKey).iterator();
									while(valIterator.hasNext()) {
										OWLNamedIndividual preference = (OWLNamedIndividual) valIterator.next();
										durationPreferences.add(preference);
									}	
								}
								if(stripFromIRI(dateKey.toString()).equals("hasTransport")) {
									Iterator<OWLIndividual> valIterator = dateProperties.get(dateKey).iterator();
									while(valIterator.hasNext()) {
										OWLNamedIndividual preference = (OWLNamedIndividual) valIterator.next();
										transportPreferences.add(preference);
									}	
								}
								
							}
							int iloscPreferencji = 0;
							int howManyDurations = 0;
							int howManyTransport = 0;
							// Iterujemy po preferencjach
							Iterator<OWLNamedIndividual> indIterator = individuals.iterator();
							while(indIterator.hasNext()) {
								OWLNamedIndividual preference = indIterator.next();
								if(isInteger(stripFromIRI(preference.toString()))) {
								} else if(!stripFromIRI(preference.toString()).equals("Bus") && !stripFromIRI(preference.toString()).equals("Train") && !stripFromIRI(preference.toString()).equals("Plane")){
									iloscPreferencji++;
								} else {
								}
								// Je¿eli wycieczka zawiera preferencje wybrana przez usera to git 
								if(datePreferences.contains(preference)) {
									howMany++;
								// je¿eli preferencja czasu siê zgadza to robimy ++
								} else if(durationPreferences.contains(preference)) {
									howManyDurations++;
								} else if(transportPreferences.contains(preference)) {
									howManyTransport++;
								}
							}
							// patrzymy czy iloœæ zgodnych preferencji jest równa liczbie preferencji wybranych przez usera
							// jezeli tak to dodajemy date to wyników
							// patrzymy tez czy wycieczka zawiera przynajmniej jedna preferencje czasu i transportu wybrana przez usera
							if(howMany == iloscPreferencji && howManyDurations >= 1 && howManyTransport >= 1) {
								indReturn.add(date);								
							}
						}
						ACLOWLMessage msgAnswer = new ACLOWLMessage(ACLMessage.QUERY_IF);
						OWLOntology answerOnto;
						answerOnto = ontologyManager.getQueryManager().prepareQueryAnswerFromInstances(indReturn, holidayOntology, myAgent);
						msgAnswer.addReceiver(new AID("client", AID.ISLOCALNAME));
						msgAnswer.setOntology(msg.getOntology());
						msgAnswer.setContentOntology(answerOnto);
						System.out.println("Agent " + getLocalName() + " answering Client");
						msgAnswer.setConversationId(msg.getConversationId());
						send(msgAnswer);
					}
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				} catch (OWLOntologyStorageException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			block();
		}
	}

}
