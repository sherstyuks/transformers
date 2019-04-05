package com.compmodel.sim.trsfr;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.compmodel.sim.trsfr.core.World;

/**
The goal of the Orgs project is to model the process of 
evolving of the life-like behavior from simple components.
The desired result is the appearance of complex structures that behave like a live organism.
Properties that are considered specific to life:
- org is comprised of multiple components interacting with each other
- org exists by its component interaction. If we remove any component,
	it will break the interaction flow and org cease to exist.
- The scope of interaction of subcomponents is org. Subcomponent of one org
	primarily interact with subcomponents of this org.
- One org can interact with another org. If there exists a permanent set of orgs
	interacting within this set only, such set can be considered as an org of a higher level.
- Hierarchy of orgs evolves by itself while the world exists. 
- Interaction between components occurs via results of their activities. 
	as in producer-consumer chain.
	Example1:
	org1{A->B_Producer, B->A_Producer}
	It is self-sufficient org. As long as object A is available to A->B_Producer component,
	org enters endless loop. 
	Self-sufficient orgs cannot evolve into more complex structures. Once activated, 
	they do not need anything, they do not interact. 
- org must have a need for resources
- Consumption of a resource must be a finite process.
	Example2:
	org1{A->B_Producer, B->C_Producer}
	org2{D->F_Producer, F->A_Producer}
	These two org can interact. 
- Probability of interaction between components depends on their proximity.
	In the Example2 if F->A_Producer creates productA far from the location of A->B_Producer,
	interaction will be less likely.
- org becomes active (live) to get a chance to perform actions. All components become active,
	and if resources are available, actions are performed.
- To implement selection, following rules will be applied:
	* after world creation orgs will be determined based on the location of producers, 
	their proximity to each other
*  	* each org will be attempted to activate. Activation score will be calculated based on the number of
		producers actually made actions.
	* After a number of activations, orgs will be ranked and the worst performers will be dispersed.
		Resource allocation may change between activations.

Notes
- We do not try to build resources. We keep the original products A,B,C, etc. 
	Operations to build/modify complex resources may be a subject of the next project
- We do not try to build producers. Complex producers dealing with complex resources 
	may be a subject of the next project.

Implementation details
- Producers and products will exist in euclidean space. 
	Dimension of the space will be a parameter.
- Probability that producer picks up a source product depends on the distance. 
	The interaction function will be a parameter
- One run activates all orgs for a period of time. During this time orgs will perform what they can.
- The producer-consumer interaction will be detected and saved in participants history. 
	Record will keep full path of the participants on both sides.
- During the run products will be reallocated randomly multiple times, to allow testing of possible 
 	combinations of producer-consumer interactions in orgs.
- After run is finished, orgs will be redefined. The algorithm is yet to be defined. Roughly:
	* If in the org non-interacted components detected, 
		remove them from the org
		and add to pool of free components of the org's owner
	* Among the active components perform dense-based clustering, using inverse frequency of interaction as a distance.
	* If among org's components external interaction has been detected,
		TBD probably need to allow org to belong to disjoint orgs.
	 	
	* 
	====== Additions after 11/08/2018 ======
	Interactions of orgs is a repeating process. Sets of interacting orgs can be in sync with each other,
	creating a resonance. Let's assume a hypothesis that resonance is a mechanism that selects
	optimal combinations of orgs. 
	
	All interactions occur by quantum of time. 
	Example of resonance.
	ResourceA is consumed by OrgAtoB producing resourceB and requiring timeAtoB time.
	ResourceB is consumed by OrgBtoA producing resourceE and requiring timeBtoA time.
	If timeAtoB == timeBtoA, we can supply resources (A.B) to the set of orgs [AtoB, BtoA]
	and they will work in sync with each other endlessly.
		AtoB: A->B;A->B;A->B;
		BtoA: B->A;B->A;B->A;
	Set [AtoB, BtoA] can be considered as a next level org. It needs resources (A,B) to live.
	
	If timeAtoB > timeBtoA,resource A will remain free and can be consumed by another org, AtoC. 
	After AtoC consumed A cycle stops.  
		AtoB: A-------->B;
		BtoA: B->A;
		AtoC:       A->C;
   
    The scenario above is not sufficient enough to suppress unsynchronized interaction,
    because it relies on the presence of alternative consumer, which is not very likely.
    To make active orgs preferable to idle ones, we add a rule: if org cannot perform an action,
    brownian motion occurs instead. 
    To counter-balance brownian motion which works as a repulsing force, 
    we also introduce an attracting force - consumer moves closer to the supplier with each interaction.
    
    With introduced repulsing and attracting forces, after some time we should expect creation of
    the simple orgs, comprised of 2-3 transformers, and a number of free transformers moving
    around randomly.
    
    The org [AtoB, BtoA] is called "closed" org, meaning it is self-sufficient, once it received 
    the initial resources. It can be active indefinitely, if nothing interferes. 
    Interference may come as competing transformer approaching and intercepting resource.
    Such events occurs on the random basis, results are unpredictable and thus cannot be
    considered as an interaction mechanism. Because of that closed orgs are not suited to be
    a building blocks for complex orgs.    
    
    "Open" orgs are those that need permanent input of resources to stay active. They are  
    consumers and producers themselves, like their constituent elements. Examples:
    [AtoB,BtoC,CtoD], [AtoC,CtoB,BtoD], [DtoB,BtoE]
    Such sets can interact with each other using the same Producer/Consumer mechanism.
    The question can be raised, why two orgs  [AtoB,BtoC,CtoD] and [DtoB,BtoE] can't be 
    considered as one [AtoB,BtoC,CtoD,DtoB,BtoE]?
    There are two possible reasons:
    1) proximity - each org is comprised from components that are close to each other.
    2) The output of [AtoB,BtoC,CtoD] can be consumed by more than one orgs.
    
    Hierarchy of orgs:
    Level 0 - elementary transformers
    Level 1 - sequences of transformers with one-dimensional exchange of single resources
    Level 2 - groups of Level1 sequences, exchanging single resources in graph.
    Level 3 - groups of Level2 orgs exchanging groups of resources in graph.
    
    The goal of the simulation is to reproduce creation of these three org levels.
    



*/
public class App 
{
	private static final Logger log = LoggerFactory.getLogger(App.class);
    public static World world;

    public static void main( String[] args ) {
		if(args.length == 0) {
			log.info("Starting world from random state with preset params");
	        world = new World();
	        world.seedTransformers();
		}else {
			log.info("Loading properties from "+args[0]);
			Properties prop = loadProps(args[0]);
			if(prop.getProperty("snapshotFile")!=null) {
				world = loadWorldSnapshot(prop.getProperty("snapshotFile"));
			}else {
				log.info("Starting world from random state");
		        world = new World();
		        world.seedTransformers();
			}
			changeSettings(world,prop);
		}
		if(world != null) {
			log.info(world.buildWorldParamsTitle());
			world.run();
		}
    }
	
	private static Properties loadProps(String fileName) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(fileName);
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
		return prop;
	}

	private static void changeSettings(World world2, Properties prop) {
		if(prop.getProperty("energyScale") != null) {
			world.setEnergyScale(Double.parseDouble(prop.getProperty("energyScale")));
		}
		if(prop.getProperty("searchDistance") != null) {
			world.setSearchDistance(Integer.parseInt(prop.getProperty("searchDistance")));
		}
		if(prop.getProperty("actionDistancePenalty") != null) {
			world.setActionDistancePenalty(Double.parseDouble(prop.getProperty("actionDistancePenalty")));
		}
		if(prop.getProperty("turnsPerSeed") != null) {
			world.setTurnsPerSeed(Integer.parseInt(prop.getProperty("turnsPerSeed")));
		}
		if(prop.getProperty("reseedPct") != null) {
			world.setReseedPct(Integer.parseInt(prop.getProperty("reseedPct")));
		}
		if(prop.getProperty("atomsNumber") != null) {
			world.setAtomsNumber(Integer.parseInt(prop.getProperty("atomsNumber")));
		}
		if(prop.getProperty("trsfrNumber") != null) {
			world.setTrsfrNumber(Integer.parseInt(prop.getProperty("trsfrNumber")));
		}
		if(prop.getProperty("massRatio") != null) {
			world.setMassRatio(Integer.parseInt(prop.getProperty("massRatio")));
		}
		if(prop.getProperty("massRatioLinked") != null) {
			world.setMassRatioLinked(Integer.parseInt(prop.getProperty("massRatioLinked")));
		}
		if(prop.getProperty("temperature") != null) {
			world.setTemperature(Integer.parseInt(prop.getProperty("temperature")));
		}
		if(prop.getProperty("idleWait") != null) {
			world.setIdleWait(Integer.parseInt(prop.getProperty("idleWait")));
		}
		if(prop.getProperty("maxFilesCnt") != null) {
			world.setMaxFilesCnt(Integer.parseInt(prop.getProperty("maxFilesCnt")));
		}
		if(prop.getProperty("saveShotPeriod") != null) {
			world.setSaveShotPeriod(Integer.parseInt(prop.getProperty("saveShotPeriod")));
		}
		if(prop.getProperty("saveSnapShotPeriod") != null) {
			world.setSaveSnapShotPeriod(Integer.parseInt(prop.getProperty("saveSnapShotPeriod")));
		}
		if(prop.getProperty("chainAnaliticsPeriod") != null) {
			world.setChainAnaliticsPeriod(Integer.parseInt(prop.getProperty("chainAnaliticsPeriod")));
		}
		if(prop.getProperty("worldAnaliticsPeriod") != null) {
			world.setWorldAnaliticsPeriod(Integer.parseInt(prop.getProperty("worldAnaliticsPeriod")));
		}
		if(prop.getProperty("collisionWinTrs") != null) {
			world.setCollisionWinTrs(Double.parseDouble(prop.getProperty("collisionWinTrs")));
		}
		if(prop.getProperty("fileDir") != null) {
			world.setFileDir(prop.getProperty("fileDir"));
		}
		world.setFileCntTransformers(0);
	}

	/**
	 * Load snapshot from file
	 * 
	 * @param file name
	 * @return
	 */
	private static World loadWorldSnapshot(String fileName) {
        try{    
            FileInputStream file = new FileInputStream(fileName); 
            ObjectInputStream in = new ObjectInputStream(file); 
            world = (World)in.readObject(); 
            in.close(); 
            file.close(); 
        }catch(IOException ex){ 
            ex.printStackTrace(); 
            return null;
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} 
		log.info("loaded snapshot {}", fileName);
     	return world;
	}

}
