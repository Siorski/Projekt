package projekt.agents;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jadeOWL.base.OntologyManager;
import jadeOWL.base.messaging.ACLOWLMessage;


public class AgentClient extends Agent{

	private static final long serialVersionUID = -3212467805232172479L;
	OntologyManager ontologyManager;
	OWLOntology holidayOntology;
	Set<OWLNamedIndividual> individuals = new HashSet<OWLNamedIndividual>();
	OWLDataFactory dataFactory;
	public static int step = 0;
	public static boolean finished = false;
	public void setStep(int step) {
		AgentClient.step = step;
	}
	@Override
	protected void setup(){
		System.out.println("Agent " + getLocalName() + " started");
		ontologyManager = new OntologyManager();
		try {
			
			holidayOntology = ontologyManager.loadAndMapOntology(new File("ontologies/holidayOntology.owl"), "http://semantykaProjekt.com/holidayOntology.owl");
			
			//Ask for all countries using custom query
			
			//Create a new empty query ontology
			OWLOntology countryQueryOntology = ontologyManager.getQueryManager().createNewOWLQueryOntology(this);
			
			//Get reference to the "Country" class
			OWLClass countryClass = ontologyManager.getDataFactory().getOWLClass(holidayOntology, "Country");
			
			//Create a query class that asks for all individuals of class "Country"
			ontologyManager.getQueryManager().createCustomQueryClass(countryQueryOntology, "countryQuery", countryClass);
			addBehaviour(new AskForCountriesBehaviour(this));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void takeDown(){
		//Clean up
		ontologyManager.removeAndUnmapAllOntologies();
	}
	protected String stripFromIRI(String s) {
		String[] temp = s.split("#");
		return temp[1].split(">")[0];
	}
	class AskForCountriesBehaviour extends Behaviour
	{
		String[] preferences = new String[15];
		private static final long serialVersionUID = 5639516177887167091L;
    	String nextQuery = null;
    	public AskForCountriesBehaviour(Agent a) {
            super(a);
        }
        @Override
		public void action() 
        {
        	ACLOWLMessage msg = new ACLOWLMessage(ACLMessage.QUERY_IF);
        	msg.addReceiver(new AID("trip", AID.ISLOCALNAME));
        	msg.setOntology("http://semantykaProjekt.com/holidayOntology.owl");
        	switch(step) {
        	case 0:
        		msg.setConversationId("Preferences");
        		System.out.println("Agent " + getLocalName() + " asking for preferences");
        		try {
					OWLOntology preferencesQueryOntology = ontologyManager.getQueryManager().createNewOWLQueryOntology();
					OWLClass preferencesClass = ontologyManager.getDataFactory().getOWLClass(holidayOntology, "Preferences");
					ontologyManager.getQueryManager().createCustomQueryClass(preferencesQueryOntology, "preferencesQuery", preferencesClass);
					msg.setContentOntology(preferencesQueryOntology);
				} catch (OWLOntologyCreationException e1) {
					e1.printStackTrace();
				} catch (OWLOntologyStorageException e) {
					e.printStackTrace();
				}
            	send(msg);
        		break;
        	case 1:
        		individuals.clear();
        		System.out.println("Agent " + getLocalName() + " asking for cities(Object properties) for preferences");
        		try {
					OWLOntology cityQueryOntology = ontologyManager.getQueryManager().createNewOWLQueryOntology();
					for(int i = 0; i < preferences.length; i++) {
						if(preferences[i]!=null && !preferences[i].isEmpty())
							individuals.add(ontologyManager.getDataFactory().getOWLNamedIndividual(holidayOntology, preferences[i]));
					}
					OWLOntology answerOnto = ontologyManager.getQueryManager().prepareQueryAnswerFromInstances(individuals, cityQueryOntology, myAgent);
					msg.setContentOntology(answerOnto);
					msg.setConversationId("City");
				} catch (IllegalArgumentException | OWLOntologyStorageException e) {
					e.printStackTrace();
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				}
        		send(msg);
        		break;
        	}
     		ACLMessage msgAnswer = blockingReceive();
    		System.out.println("Agent " + getLocalName() + " received message");
            if (msgAnswer!=null){
            	OWLOntology ontology;
				try {
					individuals.clear();
					ontology = ontologyManager.getOntologyFromACLMessage(msgAnswer);
					individuals = ontologyManager.getQueryManager().filterAnswerSetInstances(ontology);
					ontologyManager.getQueryManager().removeAnswerSetAxioms(ontology);
					switch(step) {
						case 0:
							PreferencesFrame dialog = new PreferencesFrame();
							preferences = dialog.Create(individuals);
							dialog.setVisible(true);
							dialog.setSize(500,600);
							dialog.addNotify();
							step++;
						break;
						case 1:
							TripsFrame dialog1 = new TripsFrame(individuals);
							dialog1.createAndShowGUI();
						break;
					}
				} catch (OWLOntologyCreationException | IOException e) {
					e.printStackTrace();
				}
            }
                
        }

		@Override
		public boolean done() {
			return finished;
		}
	}
}
