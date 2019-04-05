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
 *  interaction is random. Repeatable behavior is more likely in chains.
 *  
 *  For current transformers model the apparent way to select combinations - 
 *  encourage those with the longer lifetime. 
 *  
 *  The mechanism to select stable sets will be competition.
 *  
 *  For the lowest level, chains, we can suggest following competition implementations:
 *  1. Chain with stronger bonds can break another one with weaker bonds. 
 *     Collision may happen when moving. We can allow breaking when trsf is trying to move to the place in space that is
 *     already occupied. 
 *     We can limit breaking action to the situaition when end of the chain collides only.
 *  2. Bonds can weaken and disappear if no regular actions occur.
 *  
 *  The first option is potentially more interesting, because it may induce creation of spatial constructions.
 *  
 *  How it will work on other levels, and how to produce these levels - yet to be seen. 
 *  
 *
 *
 *
 *
 *
 * @author shers
 *
 */
public class AppComments {

}
