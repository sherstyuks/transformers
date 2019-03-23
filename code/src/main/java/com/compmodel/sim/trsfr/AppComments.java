package com.compmodel.sim.trsfr;
/**
 * Comments and thoughts around TRSFR program
 * 
 * Attempt 1
 * Transformers and atoms attracted to each other produced heaps of random transformers. 
 * Eventually all transformers collapsed into one big heap.
 * 
 * Attempt 2
 * Transformers could form chains. So far there are no indications that chains evolve into anything regular.
 * Average life time and match percentage does not seem to increase in count range till 15K.
 * 
 * Attempt 3
 * Goal is to build a model that will produce a limited number of stable transformer combinations.
 * Stability is proportional to the frequency of interactions between transformers.
 * Frequency can favor from atoms being contained within the set boundaries.
 * 
 * Condition on the stable combinations - they must be "open". To live they have to consume something and to produce.
 * The model to be scalable, elements that are consumed and produced must also be combinations.
 * The reason why would multiple simple combinations be better than single complex one -
 * the rate how results are produced varies. For example, product A is created at a rate 2 per sec.
 * To consume A and produce B and C requires 1 sec. In this case the set:
 *  - A producer
 *  - AtoB transformer
 *  - AtoC transformer
 *  will have most effective interaction, all three components will be busy all time.
 *  
 *  Small size of stable sets makes them easier to move around.
 *  On the other side, sets working in compact space are unlikely to exhibit repeatable behavior,
 *  interaction is random.
 *  
 *  For current transformers model the apparent way to select combinations - 
 *  encourage those with the longer lifetime. Circular chains will obviously become winners.
 *  1. Increase ACTION_DISTANCE_PENALTY - to encourage match in chans
 *  
 *  Let's call stable sets "stabs".
 *  Components of a stabs, to be produced and consumed as a whole, must be aware about container. 
 *  What does it mean? 
 * 
 * @author shers
 *
 */
public class AppComments {

}
