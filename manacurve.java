import java.util.Random;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class manacurve {
	static int DECKSIZE = 0;
	static int LANDCOUNT = 0;
	static int MAXTURN = 0;
	static int PLAY_DRAW = 0;
	static String PLAY_TYPE_TEXT = "";

	public static void main(String[] args) {
		//Parse commandline inputs
		DECKSIZE = Integer.parseInt(args[0]);
		LANDCOUNT = Integer.parseInt(args[1]);
		MAXTURN = Integer.parseInt(args[2]);
		PLAY_DRAW = Integer.parseInt(args[3]);

		switch(PLAY_DRAW){
			case 0:
				PLAY_TYPE_TEXT = "Play";
				break;
			case 1:
				PLAY_TYPE_TEXT = "Draw";
				break;
			case 2:
				PLAY_TYPE_TEXT = "Random";
				break;
			default:
				PLAY_TYPE_TEXT = "Play";
		}

		//Three Stage Hill Climbing Algorithm to find optimal manacurve

		//Do a grid search with loose mulligan rules to narrow down results
		int OptimalCurve [] = gridSearch(DECKSIZE, LANDCOUNT, MAXTURN, 5000);

		//Do a finer search using loose mulligan rules
		int NewOptimalCurve [] = looseSearch(OptimalCurve, 10000);

		//Do final search using optimal mulligan rules (resource intensive)
		optimalSearch(NewOptimalCurve, 100000);


	}//end of main

	public static int[] gridSearch (int DECKSIZE, int LANDCOUNT, int MAXTURN, int NumSimulation){
		Deck deck=new Deck();
		LondonMullChoice[][][][][][][][] KeepOpeningHand = new LondonMullChoice[8][8][8][8][8][8][8][8];
		KeepOpeningHand=GiveNoMulliganStrategy();
		int NumSimPerDeck=NumSimulation;
		double TotalManaSpent;
		double MostManaSpent=0.1;
	
		int OptimalCurve[] = new int[7];
		
		OptimalCurve[0]=LANDCOUNT;
		
		int step = DECKSIZE/20; // Larger step for larger deck sizes
	
		int curve[] = new int[7];
	
		//Doing a grid search to quickly zoom in on, hopefully, the optimal configuration)
		System.out.println("\u001B[33m" + "Wide grid search......." + "\u001B[0m");
	
		//Loop through all possible mana curve combinations (curve)
		for (curve[1]=0; curve[1]<=(DECKSIZE-OptimalCurve[0]); curve[1]+=step){
		for (curve[2]=0; curve[2]<=(DECKSIZE-OptimalCurve[0]-curve[1]); curve[2]+=step){
		for (curve[3]=0; curve[3]<=(DECKSIZE-OptimalCurve[0]-curve[1]-curve[2]); curve[3]+=step){
		for (curve[4]=0; curve[4]<=(DECKSIZE-OptimalCurve[0]-curve[1]-curve[2]-curve[3]); curve[4]+=step){
		for (curve[5]=0; curve[5]<=(DECKSIZE-OptimalCurve[0]-curve[1]-curve[2]-curve[3]-curve[4]); curve[5]+=step){
			
			curve[6] = DECKSIZE-OptimalCurve[0]-curve[1]-curve[2]-curve[3]-curve[4]-curve[5];
	
			//if maxturns is 5: don't bother with 6 drops etc.
			boolean allowed[] = new boolean [7];
	
			allowed[1] = MAXTURN<1 ? curve[1]<1 : true; 
			allowed[2] = MAXTURN<2 ? curve[2]<1 : true; 
			allowed[3] = MAXTURN<3 ? curve[3]<1 : true;
			allowed[4] = MAXTURN<4 ? curve[4]<1 : true;
			allowed[5] = MAXTURN<5 ? curve[5]<1 : true;
			allowed[6] = MAXTURN<6 ? curve[6]<step : true;
	
			if (allowed[1] &&allowed[2] &&allowed[3] &&allowed[4] &&allowed[5] &&allowed[6]) {
	
				curve[0] = OptimalCurve[0];
				
				//test current curve curve
				deck.SetDeck(curve);
				TotalManaSpent=AverageManaSpentForRandomHand(deck,7,KeepOpeningHand,NumSimPerDeck);
				
				//If total mana of current curve is better
				if (TotalManaSpent > MostManaSpent){
					MostManaSpent = TotalManaSpent;
					for (int i = 1; i < 7; i++){
						OptimalCurve[i] = curve[i];
	
					}
	
				}
	
			}     
							
		}}}}}//end of grid search
	
		printDeckResults(OptimalCurve, MostManaSpent, MAXTURN);
	
		return OptimalCurve;
	}

	public static int[] looseSearch(int OptimalCurve[], int NumSimulation){

		//Refined search loose mulligan +/- 4
		System.out.println("\u001B[33mLoose Mulligan search.......\u001B[0m");
	
		Deck deck=new Deck();
		LondonMullChoice[][][][][][][][] KeepOpeningHand = new LondonMullChoice[8][8][8][8][8][8][8][8];
		KeepOpeningHand=GiveLooseMulliganStrategy();
	
		int NumSimPerDeck=NumSimulation;
		double TotalManaSpent;
		double MostManaSpent=0.1;
	
		int NewOptimalCurve[] = new int[7];
	
		NewOptimalCurve[0] = LANDCOUNT;
		
		int buffer = 4; // How far to search from current best curve
	
		int curve[] = new int[7];
	
		//Loop through all possible cominations of curves (curve) given constraint 
		for (curve[1]=Math.max(0,OptimalCurve[1]-buffer); curve[1]<=(DECKSIZE-NewOptimalCurve[0]); curve[1]++){
		for (curve[2]=Math.max(0,OptimalCurve[2]-buffer); curve[2]<=(DECKSIZE-NewOptimalCurve[0]-curve[1]); curve[2]++){
		for (curve[3]=Math.max(0,OptimalCurve[3]-buffer); curve[3]<=(DECKSIZE-NewOptimalCurve[0]-curve[1]-curve[2]); curve[3]++){
		for (curve[4]=Math.max(0,OptimalCurve[4]-buffer); curve[4]<=(DECKSIZE-NewOptimalCurve[0]-curve[1]-curve[2]-curve[3]); curve[4]++){
		for (curve[5]=Math.max(0,OptimalCurve[5]-buffer); curve[5]<=(DECKSIZE-NewOptimalCurve[0]-curve[1]-curve[2]-curve[3]-curve[4]); curve[5]++){
			
			curve[6] = DECKSIZE-NewOptimalCurve[0]-curve[1]-curve[2]-curve[3]-curve[4]-curve[5];
							
			//Check if six drops(etc) are even possible and less than upper limit range
			boolean allowed[] = new boolean [7];
	
			for (int i = 1; i < 7; i++){
				allowed[i] = MAXTURN<i ? curve[i]==0 : curve[i]<=OptimalCurve[i]+buffer;
	
			}
	
			if (allowed[1] &&allowed[2] &&allowed[3] &&allowed[4] &&allowed[5] &&allowed[6]) {
				//if max turn is 6+ , don't bother with 1 drops (save compute time)
				if (MAXTURN > 5 ? curve[1]==0 : true){
					curve[0] = NewOptimalCurve[0];
				
					//Test current curve curve
					deck.SetDeck(curve);
					TotalManaSpent=AverageManaSpentForRandomHand(deck,7,KeepOpeningHand,NumSimPerDeck);
					
					//If current curve is better
					if (TotalManaSpent>MostManaSpent){
						MostManaSpent=TotalManaSpent;
		
						for (int i = 1; i< 7; i ++ ){
							NewOptimalCurve[i]=curve[i];
		
						}
		
					}

				}
	
			}
						 
		}}}}}//end of refined loose muligan
	
		printDeckResults(NewOptimalCurve, MostManaSpent, MAXTURN);
	
		return NewOptimalCurve;
	
	}

	public static int[] optimalSearch(int NewOptimalCurve[], int NumSimulation){
		//------------------------------------------------
		//Final search optimal mulligan
		System.out.println("\u001B[33mOptimal Mulligan search.......\u001B[0m");
	
		Deck deck=new Deck();
		LondonMullChoice[][][][][][][][] KeepOpeningHand = new LondonMullChoice[8][8][8][8][8][8][8][8];
		KeepOpeningHand=GiveLooseMulliganStrategy();
	
		int NumSimPerDeck=NumSimulation;
		double TotalManaSpent;
		double MostManaSpent=0.1;
	
		int FinalOptimalCurve[] = new int [7];
		FinalOptimalCurve[0] = LANDCOUNT;
	   
		int buffer = 1;
		int backbuffer = 1;
	
		boolean continueSearch = false;
		
		//while there is better deck to find
		do{
			int curve[] = new int[7];
		
			for (curve[1]=Math.max(0, NewOptimalCurve[1]-backbuffer); curve[1]<= (DECKSIZE-FinalOptimalCurve[0]); curve[1]++){
			for (curve[2]=Math.max(0, NewOptimalCurve[2]-backbuffer); curve[2]<= (DECKSIZE-FinalOptimalCurve[0]-curve[1]); curve[2]++){
			for (curve[3]=Math.max(0, NewOptimalCurve[3]-backbuffer); curve[3]<= (DECKSIZE-FinalOptimalCurve[0]-curve[1]-curve[2]); curve[3]++){
			for (curve[4]=Math.max(0, NewOptimalCurve[4]-backbuffer); curve[4]<= (DECKSIZE-FinalOptimalCurve[0]-curve[1]-curve[2]-curve[3]); curve[4]++){
			for (curve[5]=Math.max(0, NewOptimalCurve[5]-backbuffer); curve[5]<= (DECKSIZE-FinalOptimalCurve[0]-curve[1]-curve[2]-curve[3]-curve[4]); curve[5]++){
				
				curve[6] = DECKSIZE-FinalOptimalCurve[0]-curve[1]-curve[2]-curve[3]-curve[4]-curve[5];
								
				//if maxturns is 5: don't bother with 6 drops etc.
	
				boolean allowed[] = new boolean [7];
				for (int i = 1; i < 7; i++){

					//If max turn is 5, then number 5+ drops should be 0
					//else they should be less than buffer
					allowed[i] = MAXTURN<i ? curve[i]==0 : curve[i]<=NewOptimalCurve[i]+buffer;
	
				}
				
				
				if (allowed[1] &&allowed[2] &&allowed[3] &&allowed[4] &&allowed[5] &&allowed[6]) {
					//if max turn is 6+ , don't bother with 1 drops (save compute time)
					if (MAXTURN > 5 ? curve[1]==0 : true){
						curve[0] = FinalOptimalCurve[0];
	
						//Test current curve curve
						deck.SetDeck(curve);
						deck.PrintDeckBrief();
						KeepOpeningHand=GiveOptimalMulliganStrategy(deck);
						TotalManaSpent=AverageManaSpentForRandomHand(deck,7,KeepOpeningHand,NumSimPerDeck);
						System.out.print(" " + TotalManaSpent + "\t");
		
						//if current curve is better
						if (TotalManaSpent>MostManaSpent){
							MostManaSpent=TotalManaSpent;
		
							for (int i = 1; i < 7; i ++){
								FinalOptimalCurve[i] = curve[i];
		
							}
		
							System.out.print("*");
						}
		
						System.out.print("\t|" + MostManaSpent);
						System.out.println();

					}
	
					
				}
					
								
			}}}}}//end of optimal muligan search
	
			printDeckResults(FinalOptimalCurve, MostManaSpent, MAXTURN);
	
	
			//if new deck is the same as old deck: finish search. Else: continue searching
			if (Math.abs(FinalOptimalCurve[1]-NewOptimalCurve[1]) +
				Math.abs(FinalOptimalCurve[2]-NewOptimalCurve[2]) +
				Math.abs(FinalOptimalCurve[3]-NewOptimalCurve[3]) +
				Math.abs(FinalOptimalCurve[4]-NewOptimalCurve[4]) +
				Math.abs(FinalOptimalCurve[5]-NewOptimalCurve[5]) +
				Math.abs(FinalOptimalCurve[6]-NewOptimalCurve[6]) == 0
				){
				
				continueSearch = false;
				System.out.println("No better deck found");
	
			}else{
				continueSearch = true;
				System.out.println("Searching for better deck....");
				MostManaSpent=1;
			}
	
			for (int i =1; i < 7; i ++){
				NewOptimalCurve[i] = FinalOptimalCurve[i];
			}
	
		} while(continueSearch);
	
		System.out.println("\u001B[33mFinal Results\u001B[0m");
		printDeckResults(FinalOptimalCurve, MostManaSpent, MAXTURN);
		
	
		//Save results to file
		String logText = String.format("%d, %d, %d, %d, %d, %d, %d, %d, %.6f, %s\n", FinalOptimalCurve[1], FinalOptimalCurve[2], FinalOptimalCurve[3],FinalOptimalCurve[4],FinalOptimalCurve[5],FinalOptimalCurve[6],FinalOptimalCurve[0], MAXTURN, MostManaSpent, PLAY_TYPE_TEXT);
	
		appendResultToFile("optimal_curve_london.csv", logText);
	
		return FinalOptimalCurve;
	
	}

		
	public static void printDeckResults (int curve[], double MostManaSpent, int MaxTurns){
		Deck deck=new Deck();
		System.out.println("---------------------------------------------------------");
		System.out.print("The optimal deck after optimal mulligan search was:");
		deck.SetDeck(curve);
		System.out.println();
		System.out.println(" 1  2  3  4  5  6  L");
		deck.PrintDeckBrief();
		System.out.println();
		System.out.println("Expected mana spent: " + MostManaSpent + " after turn " + MaxTurns);
		System.out.println("---------------------------------------------------------");
		System.out.println();
	}
	

	public static void appendResultToFile(String filename, String text) {
		try{
			FileWriter fileWriter = new FileWriter(filename, true);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.printf(text);
			printWriter.close();
		}catch(IOException e){

		}
		
	}
 
    public static LondonMullChoice[][][][][][][][] GiveOptimalMulliganStrategy(Deck deck) {
		LondonMullChoice[][][][][][][][] KeepOpeningHand = new LondonMullChoice[8][8][8][8][8][8][8][8];
		OpeningHand openinghand=new OpeningHand();
		int NumSimPerOpeningHandSize=10000;
	
		int OriginalCurve[] = new int [7];
	
		OriginalCurve[1]=deck.NumberOf1Cost;
		OriginalCurve[2]=deck.NumberOf2Cost;
		OriginalCurve[3]=deck.NumberOf3Cost;
		OriginalCurve[4]=deck.NumberOf4Cost;
		OriginalCurve[5]=deck.NumberOf5Cost;
		OriginalCurve[6]=deck.NumberOf6Cost;
		OriginalCurve[0]=deck.NumberOfLands;
	
		double CutOffManaSpent = 0.1;

		int mullCutoff = (int)(MAXTURN/1.3); // Furthest Mulligan willing to go to. Can save computing resource.
	
		//Let's just keep every 4-card hand. That simplifies things.
		for (int StartingCards=mullCutoff; StartingCards<=7; StartingCards++){
			//System.out.print(".");
			//System.out.println("---------"+CutOffManaSpent);
			int lp[] = new int[7];
	
			for (lp[1]=0; lp[1]<=7; lp[1]++){
			for (lp[2]=0; lp[2]+lp[1]<=7; lp[2]++){
			for (lp[3]=0; lp[3]+lp[2]+lp[1]<=7; lp[3]++){
			for (lp[4]=0; lp[4]+lp[3]+lp[2]+lp[1]<=7; lp[4]++){
			for (lp[5]=0; lp[5]+lp[4]+lp[3]+lp[2]+lp[1]<=7; lp[5]++){
			for (lp[6]=0; lp[6]+lp[5]+lp[4]+lp[3]+lp[2]+lp[1]<=7; lp[6]++){
	
				lp[0]=7-lp[1]-lp[2]-lp[3]-lp[4]-lp[5]-lp[6];
	
				if (lp[1]<=OriginalCurve[1] && 
					lp[2]<=OriginalCurve[2] && 
					lp[3]<=OriginalCurve[3] && 
					lp[4]<=OriginalCurve[4] && 
					lp[5]<=OriginalCurve[5] && 
					lp[6]<=OriginalCurve[6] && 
					lp[0]<=OriginalCurve[0]){
	
					int mullCount = 7-StartingCards;
					double highestBotAvgMana = 0.1;
	
					int bestBottom[] = new int [7];
	
					int blp [] = new int [7];
	
					for (blp[1] = 0; blp[1]<= mullCount; blp[1]++){
					for (blp[2] = 0; blp[2] + blp[1] <= mullCount; blp[2]++){
					for (blp[3] = 0; blp[3]+blp[2]+blp[1]<=mullCount; blp[3]++){
					for (blp[4] = 0; blp[4]+blp[3]+blp[2]+blp[1] <=mullCount; blp[4]++){
					for (blp[5] = 0; blp[5]+blp[4]+blp[3]+blp[2]+blp[1]<=mullCount; blp[5]++){
					for (blp[6] = 0; blp[6]+blp[5]+blp[4]+blp[3]+blp[2]+blp[1]<=mullCount; blp[6] ++){
	
						blp[0] = mullCount - blp[1] - blp[2] - blp[3] - blp[4] - blp[5] - blp[6];
	
						if(blp[1]<=lp[1] && blp[2]<=lp[2] && blp[3] <=lp[3] && blp[4] <=lp[4] && blp[5] <=lp[5] && blp[6] <=lp[6] && blp[0] <= lp[0]){
							//
							openinghand.SetHand(lp[1]-blp[1], lp[2]-blp[2], lp[3]-blp[3], lp[4]-blp[4], lp[5]-blp[5], lp[6]-blp[6], lp[0]-blp[0]);
	
							deck.SetDeck(OriginalCurve[1]-blp[1],OriginalCurve[2]-blp[2],OriginalCurve[3]-blp[3],OriginalCurve[4]-blp[4],OriginalCurve[5]-blp[5],OriginalCurve[6]-blp[6],OriginalCurve[0]-blp[0]);
	
							double BotAvgManaSpent=AverageManaSpentForSpecificHand(deck,openinghand);
	
							if(BotAvgManaSpent > highestBotAvgMana){
								highestBotAvgMana = BotAvgManaSpent;
								bestBottom[1] = blp[1];
								bestBottom[2] = blp[2];
								bestBottom[3] = blp[3];
								bestBottom[4] = blp[4];
								bestBottom[5] = blp[5];
								bestBottom[6] = blp[6];
								bestBottom[0] = blp[0];
							}
	
						}
	
					}}}}}}//end blp loop
	
	
					if (highestBotAvgMana>CutOffManaSpent) { 
						KeepOpeningHand[StartingCards][lp[1]][lp[2]][lp[3]][lp[4]][lp[5]][lp[6]][lp[0]]=new LondonMullChoice(true, bestBottom[1],bestBottom[2],bestBottom[3],bestBottom[4],bestBottom[5],bestBottom[6],bestBottom[0]);
					}
					if (highestBotAvgMana<=CutOffManaSpent) { 
						KeepOpeningHand[StartingCards][lp[1]][lp[2]][lp[3]][lp[4]][lp[5]][lp[6]][lp[0]]=new LondonMullChoice(false, bestBottom[1],bestBottom[2],bestBottom[3],bestBottom[4],bestBottom[5],bestBottom[6],bestBottom[0]);
					}
					if (StartingCards==mullCutoff) {KeepOpeningHand[StartingCards][lp[1]][lp[2]][lp[3]][lp[4]][lp[5]][lp[6]][lp[0]]=new LondonMullChoice(true, bestBottom[1],bestBottom[2],bestBottom[3],bestBottom[4],bestBottom[5],bestBottom[6],bestBottom[0]);
					}
	
	
				}
	
			}}}}}}//end lp loop
	
			deck.SetDeck(OriginalCurve);
			if (StartingCards<7) {CutOffManaSpent=AverageManaSpentForRandomHand(deck,StartingCards,KeepOpeningHand,NumSimPerOpeningHandSize);}
		}
	
		//System.out.println("Mulligan calc finished");
		return KeepOpeningHand;
	}

	public static int getIndexOfLargest( int[] array ){
		if ( array == null || array.length == 0 ) return -1; // null or empty

		int largest = array.length -1;
		for ( int i = array.length -2; i >= 0; i-- )
		{
			if ( array[i] > array[largest] ) largest = i;
		}
		return largest; // position of the first largest found
	}
 
    public static LondonMullChoice[][][][][][][][] GiveLooseMulliganStrategy() {
        LondonMullChoice[][][][][][][][] KeepOpeningHand = new LondonMullChoice[8][8][8][8][8][8][8][8];
        for (int StartingCards=2; StartingCards<=7; StartingCards++){
            for (int OneDropCount=0; OneDropCount<=7; OneDropCount++){
                for (int TwoDropCount=0; TwoDropCount+OneDropCount<=7; TwoDropCount++){
                    for  (int ThreeDropCount=0; ThreeDropCount+TwoDropCount+OneDropCount<=7; ThreeDropCount++){
                        for  (int FourDropCount=0; FourDropCount+ThreeDropCount+TwoDropCount+OneDropCount<=7; FourDropCount++){
                            for  (int FiveDropCount=0; FiveDropCount+FourDropCount+ThreeDropCount+TwoDropCount+OneDropCount<=7; FiveDropCount++){
                                for  (int SixDropCount=0; SixDropCount+FiveDropCount+FourDropCount+ThreeDropCount+TwoDropCount+OneDropCount<=7; SixDropCount++){
                                    int LandCount=7-OneDropCount-TwoDropCount-ThreeDropCount-FourDropCount-FiveDropCount-SixDropCount;

									
									int BCount[] = new int[7];
									
									
									int tempDropCount[] = new int[6];
									tempDropCount[0] = OneDropCount;
									tempDropCount[1] = TwoDropCount;
									tempDropCount[2] = ThreeDropCount;
									tempDropCount[3] = FourDropCount;
									tempDropCount[4] = FiveDropCount;
									tempDropCount[5] = SixDropCount;

									int tempLandCount = LandCount;

									for (int i = 0; i < (7-StartingCards); i++){
										if (tempLandCount > 3){
											tempLandCount--;
											BCount[6]++;
											continue;
										}

										int indexLargest = getIndexOfLargest(tempDropCount);
										tempDropCount[indexLargest]--;
										BCount[indexLargest]++;

									}





                                    
									KeepOpeningHand[StartingCards][OneDropCount][TwoDropCount][ThreeDropCount][FourDropCount][FiveDropCount][SixDropCount][LandCount]=new LondonMullChoice(false, BCount[0],BCount[1],BCount[2],BCount[3],BCount[4],BCount[5],BCount[6]);
                                    //The simple idea is that we're keeping a hand if it contains between 2 and 5 lands and at least one spell of 3 mana or less. Also, keep any 2-card hand.


                                    if (LandCount>=2 && LandCount<=5 && (OneDropCount+TwoDropCount+ThreeDropCount)>=1) {KeepOpeningHand[StartingCards][OneDropCount][TwoDropCount][ThreeDropCount][FourDropCount][FiveDropCount][SixDropCount][LandCount]=new LondonMullChoice(true, BCount[0],BCount[1],BCount[2],BCount[3],BCount[4],BCount[5],BCount[6]);}
                                    if (StartingCards<=2) {KeepOpeningHand[StartingCards][OneDropCount][TwoDropCount][ThreeDropCount][FourDropCount][FiveDropCount][SixDropCount][LandCount]=new LondonMullChoice(true, BCount[0],BCount[1],BCount[2],BCount[3],BCount[4],BCount[5],BCount[6]);}
                                }
                            }
                        }
                    }
                }
            }
        }
        return KeepOpeningHand;
    }


	public static LondonMullChoice[][][][][][][][] GiveNoMulliganStrategy() {
        LondonMullChoice[][][][][][][][] KeepOpeningHand = new LondonMullChoice[8][8][8][8][8][8][8][8];
        for (int StartingCards=0; StartingCards<=7; StartingCards++){
            for (int OneDropCount=0; OneDropCount<=7; OneDropCount++){
                for (int TwoDropCount=0; TwoDropCount+OneDropCount<=7; TwoDropCount++){
                    for  (int ThreeDropCount=0; ThreeDropCount+TwoDropCount+OneDropCount<=7; ThreeDropCount++){
                        for  (int FourDropCount=0; FourDropCount+ThreeDropCount+TwoDropCount+OneDropCount<=7; FourDropCount++){
                            for  (int FiveDropCount=0; FiveDropCount+FourDropCount+ThreeDropCount+TwoDropCount+OneDropCount<=7; FiveDropCount++){
                                for  (int SixDropCount=0; SixDropCount+FiveDropCount+FourDropCount+ThreeDropCount+TwoDropCount+OneDropCount<=7; SixDropCount++){
                                    int LandCount=7-OneDropCount-TwoDropCount-ThreeDropCount-FourDropCount-FiveDropCount-SixDropCount;
                                    KeepOpeningHand[StartingCards][OneDropCount][TwoDropCount][ThreeDropCount][FourDropCount][FiveDropCount][SixDropCount][LandCount]=new LondonMullChoice(true, 0,0,0,0,0,0,0);
                                    //Always Keep
                                    
                                }
                            }
                        }
                    }
                }
            }
        }
        return KeepOpeningHand;
    }
   
    public static double AverageManaSpentForSpecificHand(Deck deck, OpeningHand openinghand){
        int NumberOfIterations=2000;
        Deck remainingdeck=new Deck();
        double TotalManaSpent=0;
        for (int IterationCounter=1; IterationCounter<=NumberOfIterations; IterationCounter++){
            remainingdeck.SetDeck(deck.NumberOf1Cost-openinghand.NumberOf1Cost,deck.NumberOf2Cost-openinghand.NumberOf2Cost,deck.NumberOf3Cost-openinghand.NumberOf3Cost,deck.NumberOf4Cost-openinghand.NumberOf4Cost,deck.NumberOf5Cost-openinghand.NumberOf5Cost,deck.NumberOf6Cost-openinghand.NumberOf6Cost,deck.NumberOfLands-openinghand.NumberOfLands);
            TotalManaSpent=TotalManaSpent+ManaSpent(remainingdeck,openinghand);
        }
        return (TotalManaSpent/(NumberOfIterations+0.0));
    }//end of AverageManaSpentForSpecificHand
 
    public static double AverageManaSpentForRandomHand(Deck deck, int StartingCards, LondonMullChoice[][][][][][][][] KeepOpeningHand, int NumberOfIterations){
        Deck remainingdeck=new Deck();
        double TotalManaSpent=0;
        for (int IterationCounter=1; IterationCounter<=NumberOfIterations; IterationCounter++){
            OpeningHand openinghand=GiveOpeningHandAfterMulls(deck, StartingCards, KeepOpeningHand);
            remainingdeck.SetDeck(
				deck.NumberOf1Cost-openinghand.NumberOf1Cost-openinghand.B1Cost,
				deck.NumberOf2Cost-openinghand.NumberOf2Cost-openinghand.B2Cost,
				deck.NumberOf3Cost-openinghand.NumberOf3Cost-openinghand.B3Cost,
				deck.NumberOf4Cost-openinghand.NumberOf4Cost-openinghand.B4Cost,
				deck.NumberOf5Cost-openinghand.NumberOf5Cost-openinghand.B5Cost,
				deck.NumberOf6Cost-openinghand.NumberOf6Cost-openinghand.B6Cost,
				deck.NumberOfLands-openinghand.NumberOfLands-openinghand.BLandCost);
            TotalManaSpent=TotalManaSpent+ManaSpent(remainingdeck,openinghand);
            if ( IterationCounter % 10000 == 0) {
				//System.out.print(".");
			}
        }
        return TotalManaSpent/(NumberOfIterations+0.0);
    }//end of AverageManaSpentForRandomHand
   
    static OpeningHand GiveOpeningHandAfterMulls (Deck deck, int StartingCards, LondonMullChoice[][][][][][][][] KeepOpeningHand) {
       
        Deck remainingdeck=new Deck();
        OpeningHand openinghand=new OpeningHand();
        int TypeOfCardDrawn;
        boolean KeepHand=false;
       
        for (int OpeningHandSize=7; OpeningHandSize>=1; OpeningHandSize--){
            if (KeepHand==false && StartingCards>=OpeningHandSize){
                openinghand.ResetHand();
                remainingdeck.SetDeck(deck.NumberOf1Cost,deck.NumberOf2Cost,deck.NumberOf3Cost,deck.NumberOf4Cost,deck.NumberOf5Cost,deck.NumberOf6Cost,deck.NumberOfLands);
                for (int CardsDrawn=0; CardsDrawn<7; CardsDrawn++){
                    TypeOfCardDrawn=remainingdeck.DrawCard();
                    if (TypeOfCardDrawn==1) {openinghand.NumberOf1Cost++;}
                    if (TypeOfCardDrawn==2) {openinghand.NumberOf2Cost++;}
                    if (TypeOfCardDrawn==3) {openinghand.NumberOf3Cost++;}
                    if (TypeOfCardDrawn==4) {openinghand.NumberOf4Cost++;}
                    if (TypeOfCardDrawn==5) {openinghand.NumberOf5Cost++;}
                    if (TypeOfCardDrawn==6) {openinghand.NumberOf6Cost++;}
                    if (TypeOfCardDrawn==9) {openinghand.NumberOfLands++;}
                }
                KeepHand=true;
                if (OpeningHandSize>1) {
                    if (KeepOpeningHand[OpeningHandSize][openinghand.NumberOf1Cost][openinghand.NumberOf2Cost][openinghand.NumberOf3Cost][openinghand.NumberOf4Cost][openinghand.NumberOf5Cost][openinghand.NumberOf6Cost][openinghand.NumberOfLands].getKeepOpeningHand()==false) {
						KeepHand=false;
					}else{
						//adjust opening hand by bottoming cards based on mulligan strat
						int BOneDC = KeepOpeningHand[OpeningHandSize][openinghand.NumberOf1Cost][openinghand.NumberOf2Cost][openinghand.NumberOf3Cost][openinghand.NumberOf4Cost][openinghand.NumberOf5Cost][openinghand.NumberOf6Cost][openinghand.NumberOfLands].getNum1CostBottom();

						int BTwoDC = KeepOpeningHand[OpeningHandSize][openinghand.NumberOf1Cost][openinghand.NumberOf2Cost][openinghand.NumberOf3Cost][openinghand.NumberOf4Cost][openinghand.NumberOf5Cost][openinghand.NumberOf6Cost][openinghand.NumberOfLands].getNum2CostBottom();

						int BThreeDC = KeepOpeningHand[OpeningHandSize][openinghand.NumberOf1Cost][openinghand.NumberOf2Cost][openinghand.NumberOf3Cost][openinghand.NumberOf4Cost][openinghand.NumberOf5Cost][openinghand.NumberOf6Cost][openinghand.NumberOfLands].getNum3CostBottom();

						int BFourDC = KeepOpeningHand[OpeningHandSize][openinghand.NumberOf1Cost][openinghand.NumberOf2Cost][openinghand.NumberOf3Cost][openinghand.NumberOf4Cost][openinghand.NumberOf5Cost][openinghand.NumberOf6Cost][openinghand.NumberOfLands].getNum4CostBottom();

						int BFiveDC = KeepOpeningHand[OpeningHandSize][openinghand.NumberOf1Cost][openinghand.NumberOf2Cost][openinghand.NumberOf3Cost][openinghand.NumberOf4Cost][openinghand.NumberOf5Cost][openinghand.NumberOf6Cost][openinghand.NumberOfLands].getNum5CostBottom();

						int BSixDC = KeepOpeningHand[OpeningHandSize][openinghand.NumberOf1Cost][openinghand.NumberOf2Cost][openinghand.NumberOf3Cost][openinghand.NumberOf4Cost][openinghand.NumberOf5Cost][openinghand.NumberOf6Cost][openinghand.NumberOfLands].getNum6CostBottom();

						int BLandDC = KeepOpeningHand[OpeningHandSize][openinghand.NumberOf1Cost][openinghand.NumberOf2Cost][openinghand.NumberOf3Cost][openinghand.NumberOf4Cost][openinghand.NumberOf5Cost][openinghand.NumberOf6Cost][openinghand.NumberOfLands].getNumLandBottom();

						openinghand.NumberOf1Cost = openinghand.NumberOf1Cost - BOneDC;
						openinghand.NumberOf2Cost = openinghand.NumberOf2Cost - BTwoDC;
						openinghand.NumberOf3Cost = openinghand.NumberOf3Cost - BThreeDC;
						openinghand.NumberOf4Cost = openinghand.NumberOf4Cost - BFourDC;
						openinghand.NumberOf5Cost = openinghand.NumberOf5Cost - BFiveDC;
						openinghand.NumberOf6Cost = openinghand.NumberOf6Cost - BSixDC;
						openinghand.NumberOfLands = openinghand.NumberOfLands - BLandDC;

						openinghand.B1Cost = BOneDC;
						openinghand.B2Cost = BTwoDC;
						openinghand.B3Cost = BThreeDC;
						openinghand.B4Cost = BFourDC;
						openinghand.B5Cost = BFiveDC;
						openinghand.B6Cost = BSixDC;
						openinghand.BLandCost = BLandDC;



					}

                }
            }
        }
       
        return openinghand;
    }//end of GiveOpeningHandAfterMulls
    
    static int ManaSpent(Deck remainingdeck, OpeningHand openinghand) {
        
        int TotalManaSpent=0;
        //Here we put in the speed of the format
        int FinalTurn = MAXTURN;
        
            int Turn=0;
            int ManaLeft;
            int TypeOfCardDrawn;
    
            int LandsInPlay=0;
            
           
            int OneDropsInHand=openinghand.NumberOf1Cost;
            int TwoDropsInHand=openinghand.NumberOf2Cost;
            int ThreeDropsInHand=openinghand.NumberOf3Cost;
            int FourDropsInHand=openinghand.NumberOf4Cost;
            int FiveDropsInHand=openinghand.NumberOf5Cost;
            int SixDropsInHand=openinghand.NumberOf6Cost;
            int LandsInHand=openinghand.NumberOfLands;
        
			Random rand = new Random();
			boolean isOnTheDraw = false;

			switch (PLAY_DRAW){
				case 0:
					isOnTheDraw = false;
					break;
				case 1:
					isOnTheDraw = true;
					break;
				case 2:
					isOnTheDraw = rand.nextBoolean();
					break;
				default:
					isOnTheDraw = false;

			}
            
            do {
                
                Turn++;
                
                if (Turn==1) {
					if (isOnTheDraw){
						TypeOfCardDrawn=remainingdeck.DrawCard();
                    	if (TypeOfCardDrawn==1) {OneDropsInHand++;}
                    	if (TypeOfCardDrawn==2) {TwoDropsInHand++;}
                    	if (TypeOfCardDrawn==3) {ThreeDropsInHand++;}
                    	if (TypeOfCardDrawn==4) {FourDropsInHand++;}
						if (TypeOfCardDrawn==5) {FiveDropsInHand++;}
						if (TypeOfCardDrawn==6) {SixDropsInHand++;}                
                    	if (TypeOfCardDrawn==9) {LandsInHand++;}
					}

                    if (LandsInHand>=1) {LandsInPlay++; LandsInHand--;}
                    ManaLeft=LandsInPlay;
                    if (OneDropsInHand>=1 && ManaLeft==1) {
						TotalManaSpent=TotalManaSpent+1; 
						ManaLeft--; 
						OneDropsInHand--;
					}
                    //TotalManaSpent=TotalManaSpent+(LandsInPlay-ManaLeft)*0.5;
                } //end of the first turn
                
                if (Turn>1) {
 
                    TypeOfCardDrawn=remainingdeck.DrawCard();
                    if (TypeOfCardDrawn==1) {OneDropsInHand++;}
                    if (TypeOfCardDrawn==2) {TwoDropsInHand++;}
                    if (TypeOfCardDrawn==3) {ThreeDropsInHand++;}
                    if (TypeOfCardDrawn==4) {FourDropsInHand++;}
                    if (TypeOfCardDrawn==5) {FiveDropsInHand++;}
                    if (TypeOfCardDrawn==6) {SixDropsInHand++;}                
                    if (TypeOfCardDrawn==9) {LandsInHand++;}
 
                    if (LandsInHand>=1) {LandsInPlay++; LandsInHand--;}
                    ManaLeft=LandsInPlay;
					

					int CastableFiveAndTwoDrop=Math.min( Math.min(FiveDropsInHand, TwoDropsInHand), ManaLeft/7);
					if (CastableFiveAndTwoDrop>=1) {
						TotalManaSpent=TotalManaSpent+7*CastableFiveAndTwoDrop; 
						ManaLeft=ManaLeft-7*CastableFiveAndTwoDrop; 
						FiveDropsInHand=FiveDropsInHand-CastableFiveAndTwoDrop; 
						TwoDropsInHand=TwoDropsInHand-CastableFiveAndTwoDrop;
					}

					int CastableFourAndThreeDrop=Math.min( Math.min(FourDropsInHand, ThreeDropsInHand), ManaLeft/7);
					if (CastableFourAndThreeDrop>=1) {
						TotalManaSpent=TotalManaSpent+7*CastableFourAndThreeDrop; 
						ManaLeft=ManaLeft-7*CastableFourAndThreeDrop; 
						FourDropsInHand=FourDropsInHand-CastableFourAndThreeDrop; 
						ThreeDropsInHand=ThreeDropsInHand-CastableFourAndThreeDrop;
					}

                
                    int CastableSixDrops=Math.min(SixDropsInHand, ManaLeft/6);
                    if (CastableSixDrops>=1) {
						TotalManaSpent=TotalManaSpent+6*CastableSixDrops; 
						ManaLeft=ManaLeft-6*CastableSixDrops; 
						SixDropsInHand=SixDropsInHand-CastableSixDrops;
					}

					int CastableDoubleThreeDrops=Math.min(ThreeDropsInHand/2, ManaLeft/6);
					if (CastableDoubleThreeDrops>=1) {
						TotalManaSpent=TotalManaSpent+6*CastableDoubleThreeDrops; 
						ManaLeft=ManaLeft-6*CastableDoubleThreeDrops; 
						ThreeDropsInHand=ThreeDropsInHand-(CastableDoubleThreeDrops*2);
					}

					int CastableFourAndTwoDrop=Math.min( Math.min(FourDropsInHand, TwoDropsInHand), ManaLeft/6);
					if (CastableFourAndTwoDrop>=1) {
						TotalManaSpent=TotalManaSpent+6*CastableFourAndTwoDrop; 
						ManaLeft=ManaLeft-6*CastableFourAndTwoDrop; 
						FourDropsInHand=FourDropsInHand-CastableFourAndTwoDrop; 
						TwoDropsInHand=TwoDropsInHand-CastableFourAndTwoDrop;
					}

                    int CastableFiveDrops=Math.min(FiveDropsInHand, ManaLeft/5);
                    if (CastableFiveDrops>=1) {
						TotalManaSpent=TotalManaSpent+5*CastableFiveDrops; 
						ManaLeft=ManaLeft-5*CastableFiveDrops; 
						FiveDropsInHand=FiveDropsInHand-CastableFiveDrops;
					}

					int CastableThreeAndTwoDrop=Math.min( Math.min(ThreeDropsInHand, TwoDropsInHand), ManaLeft/5);
					if (CastableThreeAndTwoDrop>=1) {
						TotalManaSpent=TotalManaSpent+5*CastableThreeAndTwoDrop; 
						ManaLeft=ManaLeft-5*CastableThreeAndTwoDrop; 
						ThreeDropsInHand=ThreeDropsInHand-CastableThreeAndTwoDrop; 
						TwoDropsInHand=TwoDropsInHand-CastableThreeAndTwoDrop;
					}

                    int CastableFourDrops=Math.min(FourDropsInHand, ManaLeft/4);
                    if (CastableFourDrops>=1) {
						TotalManaSpent=TotalManaSpent+4*CastableFourDrops; 
						ManaLeft=ManaLeft-4*CastableFourDrops; 
						FourDropsInHand=FourDropsInHand-CastableFourDrops;
					}

					int CastableDoubleTwoDrops=Math.min(TwoDropsInHand/2, ManaLeft/4);
					if (CastableDoubleTwoDrops>=1) {
						TotalManaSpent=TotalManaSpent+4*CastableDoubleTwoDrops; 
						ManaLeft=ManaLeft-4*CastableDoubleTwoDrops; 
						TwoDropsInHand=TwoDropsInHand-(CastableDoubleTwoDrops*2);
					}

                    int CastableThreeDrops=Math.min(ThreeDropsInHand, ManaLeft/3);
                    if (CastableThreeDrops>=1) {
						TotalManaSpent=TotalManaSpent+3*CastableThreeDrops; 
						ManaLeft=ManaLeft-3*CastableThreeDrops; 
						ThreeDropsInHand=ThreeDropsInHand-CastableThreeDrops;
					}
                
                    int CastableTwoDrops=Math.min(TwoDropsInHand, ManaLeft/2);
                    if (CastableTwoDrops>=1) {
						TotalManaSpent=TotalManaSpent+2*CastableTwoDrops; 
						ManaLeft=ManaLeft-2*CastableTwoDrops; 
						TwoDropsInHand=TwoDropsInHand-CastableTwoDrops;
					}
                
                    int CastableOneDrops=Math.min(OneDropsInHand, ManaLeft);
                    if (CastableOneDrops>=1) {
						TotalManaSpent=TotalManaSpent+CastableOneDrops; 
						ManaLeft=ManaLeft-CastableOneDrops; 
						OneDropsInHand=OneDropsInHand-CastableOneDrops;
					}
                    
              
                } //end of a turn in which we drew a card and attacked
            } while (Turn<FinalTurn);
        
        return TotalManaSpent;
    }//end of TurnKill
 
}//End of class manacurve
 
class OpeningHand {
    int NumberOf1Cost;
    int NumberOf2Cost;
    int NumberOf3Cost;
    int NumberOf4Cost;
    int NumberOf5Cost;
    int NumberOf6Cost;
    int NumberOfLands;

	int B1Cost;
	int B2Cost;
	int B3Cost;
	int B4Cost;
	int B5Cost;
	int B6Cost;
	int BLandCost;
    
    void ResetHand(){
        NumberOf1Cost=0;
        NumberOf2Cost=0;
        NumberOf3Cost=0;
        NumberOf4Cost=0;
        NumberOf5Cost=0;
        NumberOf6Cost=0;
        NumberOfLands=0;

		B1Cost=0;
		B2Cost=0;
		B3Cost=0;
		B4Cost=0;
		B5Cost=0;
		B6Cost=0;
		BLandCost=0;
    }
           
    void SetHand (int Nr1Cost, int Nr2Cost, int Nr3Cost, int Nr4Cost, int Nr5Cost, int Nr6Cost, int NrLands) {
        NumberOf1Cost=Nr1Cost;
        NumberOf2Cost=Nr2Cost;
        NumberOf3Cost=Nr3Cost;
        NumberOf4Cost=Nr4Cost;
        NumberOf5Cost=Nr5Cost;
        NumberOf6Cost=Nr6Cost;
        NumberOfLands=NrLands;
    }

	void SetBottomedCards(int Nr1Cost, int Nr2Cost, int Nr3Cost, int Nr4Cost, int Nr5Cost, int Nr6Cost, int NrLands){
		B1Cost=Nr1Cost;
        B2Cost=Nr2Cost;
        B3Cost=Nr3Cost;
        B4Cost=Nr4Cost;
        B5Cost=Nr5Cost;
        B6Cost=Nr6Cost;
        BLandCost=NrLands;
	}
 
}//end of OpeningHand
 
class Deck {
    int NumberOf1Cost;
    int NumberOf2Cost;
    int NumberOf3Cost;
    int NumberOf4Cost;
    int NumberOf5Cost;
    int NumberOf6Cost;
    int NumberOfLands;
 
    void PrintDeckBrief () {
        if(NumberOf1Cost<10) {System.out.print("0");}
        System.out.print(NumberOf1Cost+" ");
        if(NumberOf2Cost<10) {System.out.print("0");}
        System.out.print(NumberOf2Cost+" ");
        if(NumberOf3Cost<10) {System.out.print("0");}
        System.out.print(NumberOf3Cost+" ");
        if(NumberOf4Cost<10) {System.out.print("0");}
        System.out.print(NumberOf4Cost+" ");
        if(NumberOf5Cost<10) {System.out.print("0");}
        System.out.print(NumberOf5Cost+" ");
        if(NumberOf6Cost<10) {System.out.print("0");}
        System.out.print(NumberOf6Cost+" ");
        if(NumberOfLands<10) {System.out.print("0");}
        System.out.print(NumberOfLands);
        System.out.print(" ");
    }
 
    void SetDeck (int Nr1Cost, int Nr2Cost, int Nr3Cost, int Nr4Cost, int Nr5Cost, int Nr6Cost, int NrLands) {
        NumberOf1Cost=Nr1Cost;
        NumberOf2Cost=Nr2Cost;
        NumberOf3Cost=Nr3Cost;
        NumberOf4Cost=Nr4Cost;
        NumberOf5Cost=Nr5Cost;
        NumberOf6Cost=Nr6Cost;
        NumberOfLands=NrLands;
    }

	void SetDeck (int curve[]){
		NumberOf1Cost=curve[1];
        NumberOf2Cost=curve[2];
        NumberOf3Cost=curve[3];
        NumberOf4Cost=curve[4];
        NumberOf5Cost=curve[5];
        NumberOf6Cost=curve[6];
        NumberOfLands=curve[0];
	}
    
    int NrOfCards(){
        return NumberOf1Cost+NumberOf2Cost+NumberOf3Cost+NumberOf4Cost+NumberOf5Cost+NumberOf6Cost+NumberOfLands;
    }
    
    int DrawCard (){
            Random generator = new Random();
            int RandomIntegerBetweenOneAndDeckSize=generator.nextInt( this.NrOfCards() )+1; 
            int CardType=0;
            int OneCostCutoff=NumberOf1Cost;
            int TwoCostCutoff=OneCostCutoff+NumberOf2Cost;
            int ThreeCostCutoff=TwoCostCutoff+NumberOf3Cost;
            int FourCostCutoff=ThreeCostCutoff+NumberOf4Cost;
            int FiveCostCutoff=FourCostCutoff+NumberOf5Cost;
            int SixCostCutoff=FiveCostCutoff+NumberOf6Cost;
            int LandCutoff=SixCostCutoff+NumberOfLands;
            
            if (RandomIntegerBetweenOneAndDeckSize<=OneCostCutoff) {CardType=1; this.NumberOf1Cost--;}
            if (RandomIntegerBetweenOneAndDeckSize>OneCostCutoff && RandomIntegerBetweenOneAndDeckSize<=TwoCostCutoff) {CardType=2; this.NumberOf2Cost--;}
            if (RandomIntegerBetweenOneAndDeckSize>TwoCostCutoff && RandomIntegerBetweenOneAndDeckSize<=ThreeCostCutoff) {CardType=3; this.NumberOf3Cost--;}
            if (RandomIntegerBetweenOneAndDeckSize>ThreeCostCutoff && RandomIntegerBetweenOneAndDeckSize<=FourCostCutoff) {CardType=4; this.NumberOf4Cost--;}
            if (RandomIntegerBetweenOneAndDeckSize>FourCostCutoff && RandomIntegerBetweenOneAndDeckSize<=FiveCostCutoff) {CardType=5; this.NumberOf5Cost--;}
            if (RandomIntegerBetweenOneAndDeckSize>FiveCostCutoff && RandomIntegerBetweenOneAndDeckSize<=SixCostCutoff) {CardType=6; this.NumberOf6Cost--;}
            if (RandomIntegerBetweenOneAndDeckSize>SixCostCutoff && RandomIntegerBetweenOneAndDeckSize<=LandCutoff) {CardType=9; this.NumberOfLands--;}
            
            return CardType;
    }
    
}//end of Deck

class LondonMullChoice{
	boolean keepOpeningHand;
	int num1CostBottom;
	int num2CostBottom;
	int num3CostBottom;
	int num4CostBottom;
	int num5CostBottom;
	int num6CostBottom;
	int numLandBottom;

	LondonMullChoice(boolean keepOpeningHand, 
					int num1CostBottom, 
					int num2CostBottom,
					int num3CostBottom,
					int num4CostBottom,
					int num5CostBottom,
					int num6CostBottom,
					int numLandBottom){
		
		this.keepOpeningHand = keepOpeningHand;
		this.num1CostBottom = num1CostBottom;
		this.num2CostBottom = num2CostBottom;
		this.num3CostBottom = num3CostBottom;
		this.num4CostBottom = num4CostBottom;
		this.num5CostBottom = num5CostBottom;
		this.num6CostBottom = num6CostBottom;
		this.numLandBottom = numLandBottom;


	}

	boolean getKeepOpeningHand(){return keepOpeningHand;}
	int getNum1CostBottom(){return num1CostBottom;}
	int getNum2CostBottom(){return num2CostBottom;}
	int getNum3CostBottom(){return num3CostBottom;}
	int getNum4CostBottom(){return num4CostBottom;}
	int getNum5CostBottom(){return num5CostBottom;}
	int getNum6CostBottom(){return num6CostBottom;}
	int getNumLandBottom(){return numLandBottom;}





}
