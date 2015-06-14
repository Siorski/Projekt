package projekt.agents;

import jadeOWL.base.OntologyManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class ProcessRequests {

	OntologyManager ontologyManager;
	OWLOntology holidayOntology;
	public ProcessRequests() {
		ontologyManager = new OntologyManager();
		try {
			holidayOntology = ontologyManager.loadAndMapOntology(new File("ontologies/holidayOntology.owl"), "http://semantykaProjekt.com/holidayOntology.owl");
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	//pobieramy ObjectProperties z ontologii
	public Map<OWLObjectPropertyExpression, Set<OWLIndividual>> getObjectProperties(OWLNamedIndividual individual) {
		return individual.getObjectPropertyValues(holidayOntology);
	}
	//pobieramy DataPropertiers z ontologii
	public Map<OWLDataPropertyExpression, Set<OWLLiteral>> getDataProperties(OWLNamedIndividual individual) {
		return individual.getDataPropertyValues(holidayOntology);
	}
	//pobieramy individuale z ontologii
	public OWLNamedIndividual getIndividual(String i) {
		return ontologyManager.getDataFactory().getOWLNamedIndividual(holidayOntology, i);
	}
	public String stripFromIRI(String s) {
		String[] temp = s.split("#");
		return temp[1].split(">")[0];
	}
	public String addIRI(String s) {
		return "<http://semantykaProjekt.com/holidayOntology.owl#" + s + ">";
	}
	public Set<OWLNamedIndividual> getIndividualsFromClass(String s) {
		OWLClass c = ontologyManager.getDataFactory().getOWLClass(holidayOntology, s);
		try {
			return ontologyManager.getQueryManager().getInstancesForClassQuery(c, holidayOntology);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
	//pobieramy wartoœci
	public Map<OWLDataPropertyExpression, Set<OWLLiteral>> getDataValues(OWLNamedIndividual individual) {
		return individual.getDataPropertyValues(holidayOntology); 
	}
	public String getPrice(String s) {
		String[] temp = s.split("\""); //zostawiamy tylko to co jest miêdzy " "
		return temp[1];
	}
}
