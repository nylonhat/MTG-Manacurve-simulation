# MTG-Manacurve-simulation
MTG Manacurve simulations inspired by Frank Karsten

I did some mana curve simulations and would like to share them in case others find it useful.
https://docs.google.com/spreadsheets/d/1KNF2Mni1NNenwEKamN0EArrXEJ50AtkmvVTCVfRLNbg/edit#gid=0

The simulations are inspired and are an extension of Frank Karsten's work:
https://strategy.channelfireball.com/all-strategy/mtg/channelmagic-articles/frank-analysis-finding-the-optimal-mana-curve-via-computer-simulation/

They optimise  the amount of mana spent up to a certain turn given a deck size and land count. Each mana is weighted equally regardless of the turn spent on (unless otherwise stated). The London Mulligan is taken into account by simulating all possible hand types and generating a mulligan strategy based on maximising mana spent.

Order refers to play order: Going first (play), Going second (draw), Game 1 / Going first 50% of the time (random).

**How to use the data:**

The curves tell you how many cards to play for each expected mana cost. (The amount of mana you would ideally spend on a certain card)

Each specific card should only count towards 1 specific instance in the mana curve. If a card can be cast at different expected mana costs, assign it an ideal casting cost based on your best judgement. Multiple copies of the same card don’t necessarily have to be assigned the same expected mana cost. You may decide that a proportion of the time they are cast with one cost as opposed to another; whereby you would assign them accordingly.

You may choose to assign a lower casting cost card a higher expected mana cost if you think it can generate the same value. This is common for removal spells, whereby a 2 mana card can remove a 3 mana card. In this case you may choose to treat this card as a 3 mana card in the curve. This has the effect of lowering your perceived curve but keeping the value generated the same; useful for control decks.

Essentially free spells like Gitaxian Probe, Street Wraith or Mishra's Bauble can be treated as if they decreased your total deck size. For example a play set of all 3 would make your deck a 48 card deck instead of 60; for simulation purposes.

How many lands? The curves with the highest average mana spent have higher land counts than you might expect. This is because the simulation only maximize up to a certain turn and then after that it doesn’t take into account if you get flooded or run out of cards to play. If every card in your deck has some way to use additional mana (activated abilities, card draw/generation, alternative casting costs, spell lands etc.) then getting flooded is not an issue. The fewer of these types of cards in your deck, the lower your land count may need to be.

**Limitations:**

Magic is obviously not a game just about mana curves, and simulations like these can’t possibly take into account everything. These types of analysis are just guidance for narrow aspects of the game and should be used with good judgement and consideration.

Weighing mana spent on each turn of the game equally may not be the best way. It tends to favour higher costed cards especially when optimism for later turns. However an argument could be made that if a higher costed card can’t generate enough value to deal with the same amount of mana spent spread across earlier turns, then the card may not be worth playing.

Cards that ramp mana and draw additional cards aren’t considered in these simulations. If you can get enough cards with the same ramp effect at a mana slot, you could choose to for example: ramp on turn 2 and then treat 4 mana cost cards as your 3 cost slot in the curve.

Low cost combat tricks and removal spells that can’t be played as soon as possible for their casting cost are difficult to assign on the curve. It may be useful to think about the amount of mana value they generate, even if you don’t use all your mana when you play them.

In limited formats, you should always consider card quality before considering mana curve. Only take mana curve into consideration when comparing cards of similar quality. (The same can be said for constructed formats)

The London Mulligan strategy from simulation tends to mulligan quite aggressively when optimising for a small number of turns; sacrificing some card advantage to optimise for mana spent.

This simulation doesn't take into account Best of 1 hand smoothing algorithm for Arena.


**Compiling code:**
```
javac optimalmanacurve.java
```

**Running code:**
```
Usage:
  java optimalmanacurve.java <Deck size> <Land count> <Turn> <Order>

Where:
  Order: [0|1|2]
    0: Play
    1: Draw
    2: Random
```
