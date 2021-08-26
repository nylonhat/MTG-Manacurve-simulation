import java.util.Arrays.*;
import java.util.Random;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

 
 
public class optimalmanacurve {
	static int TOTALCARDSINDECK = 0;
	static int TOTALLANDSINDECK = 0;
	static int MAXTURN = 0;
	static int PLAY_DRAW = 0;
 
    public static void main(String[] args) {
		//command line arguments
		//command line arguments
		
		MAXTURN = Integer.parseInt(args[7]);
		PLAY_DRAW = Integer.parseInt(args[8]);

		String playTypeText;
		switch(PLAY_DRAW){
			case 0:
				playTypeText = "Play";
				break;
			case 1:
				playTypeText = "Draw";
				break;
			case 2:
				playTypeText = "Random";
				break;
			default:
				playTypeText = "Play";
		}

        
        Deck deck=new Deck();
        LondonMullChoice[][][][][][][][] KeepOpeningHand = new LondonMullChoice[8][8][8][8][8][8][8][8];
        int NumberOfSimulationsPerDeck=1000000;
        double TotalManaSpent;
        double MostManaSpent=1;
        int OptimalOneDrops=Integer.parseInt(args[0]);
        int OptimalTwoDrops=Integer.parseInt(args[1]);
        int OptimalThreeDrops=Integer.parseInt(args[2]);
        int OptimalFourDrops=Integer.parseInt(args[3]);
        int OptimalFiveDrops=Integer.parseInt(args[4]);
        int OptimalSixDrops=Integer.parseInt(args[5]);
        int OptimalLands=Integer.parseInt(args[6]);
		int NumberOfCardsInDeck=OptimalOneDrops + OptimalTwoDrops+OptimalThreeDrops + OptimalFourDrops+ OptimalFiveDrops + OptimalSixDrops;
		
		int step = NumberOfCardsInDeck/20;
		
		
		//Doing a grid search (increasing each card by 3) to quickly zoom in on, hopefully, the optimal configuration)
		deck.SetDeck(OptimalOneDrops,OptimalTwoDrops,OptimalThreeDrops,OptimalFourDrops,OptimalFiveDrops,OptimalSixDrops,OptimalLands);
		//deck.PrintDeckBrief();
		//KeepOpeningHand=GiveOptimalMulliganStrategy(deck);
		KeepOpeningHand=GiveOptimalMulliganStrategy(deck);
		MostManaSpent=AverageManaSpentForRandomHand(deck,7,KeepOpeningHand,NumberOfSimulationsPerDeck);
		//System.out.print(" " + TotalManaSpent + "\t");
		
       
        System.out.println("----------------------------------");
        System.out.print("The deck after grid search was:");
        deck.SetDeck(OptimalOneDrops,OptimalTwoDrops,OptimalThreeDrops,OptimalFourDrops,OptimalFiveDrops,OptimalSixDrops,OptimalLands);
		System.out.println();
		System.out.println(" 1  2  3  4  5  6  L");
		deck.PrintDeckBrief();
        System.out.println();
        System.out.println("Expected mana spent: " + MostManaSpent + " after turn " + MAXTURN);
		System.out.println("----------------------------------");

		String logText = String.format("%d, %d, %d, %d, %d, %d, %d, %d, %.6f, %s\n", OptimalOneDrops, OptimalTwoDrops, OptimalThreeDrops,OptimalFourDrops,OptimalFiveDrops,OptimalSixDrops,OptimalLands, MAXTURN, MostManaSpent, playTypeText);

		appendResultToFile("optimal_curve_specific_london.csv", logText);
       
    }//end of main

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
        int NumberOfSimulationsPerOpeningHandSize=100000;
        int OriginalNr1Cost=deck.NumberOf1Cost;
        int OriginalNr2Cost=deck.NumberOf2Cost;
        int OriginalNr3Cost=deck.NumberOf3Cost;
        int OriginalNr4Cost=deck.NumberOf4Cost;
        int OriginalNr5Cost=deck.NumberOf5Cost;
        int OriginalNr6Cost=deck.NumberOf6Cost;
        int OriginalNrLands=deck.NumberOfLands;
        double CutOffManaSpent = 1;
        //Let's just keep every 2-card hand. That simplifies things.
        for (int StartingCards=2; StartingCards<=7; StartingCards++){
            //System.out.print(".");
			//System.out.println("---------"+CutOffManaSpent);
            for (int OneDropCount=0; OneDropCount<=7; OneDropCount++){
                for (int TwoDropCount=0; TwoDropCount+OneDropCount<=7; TwoDropCount++){
                    for  (int ThreeDropCount=0; ThreeDropCount+TwoDropCount+OneDropCount<=7; ThreeDropCount++){
                        for  (int FourDropCount=0; FourDropCount+ThreeDropCount+TwoDropCount+OneDropCount<=7; FourDropCount++){
                            for  (int FiveDropCount=0; FiveDropCount+FourDropCount+ThreeDropCount+TwoDropCount+OneDropCount<=7; FiveDropCount++){
                                for  (int SixDropCount=0; SixDropCount+FiveDropCount+FourDropCount+ThreeDropCount+TwoDropCount+OneDropCount<=7; SixDropCount++){
                                    int LandCount=7-OneDropCount-TwoDropCount-ThreeDropCount-FourDropCount-FiveDropCount-SixDropCount;
                                    if (OneDropCount<=OriginalNr1Cost && TwoDropCount<=OriginalNr2Cost && ThreeDropCount<=OriginalNr3Cost && FourDropCount<=OriginalNr4Cost && FiveDropCount<=OriginalNr5Cost && SixDropCount<=OriginalNr6Cost && LandCount<=OriginalNrLands){

										int mullCount = 7-StartingCards;
										double highestBotAvgMana = 1;
										int bestBOneDC = 0;
										int bestBTwoDC = 0;
										int bestBThreeDC = 0;
										int bestBFourDC = 0;
										int bestBFiveDC = 0;
										int bestBSixDC = 0;
										int bestBLandDC = 0;

										for (int BOneDC = 0; BOneDC<= mullCount; BOneDC++){
											for(int BTwoDC = 0; BTwoDC + BOneDC <= mullCount; BTwoDC++){
												for(int BThreeDC = 0; BThreeDC+BTwoDC+BOneDC<=mullCount; BThreeDC++){
													for(int BFourDC =0; BFourDC+BThreeDC+BTwoDC+BOneDC <=mullCount; BFourDC++){
														for (int BFiveDC =0; BFiveDC+BFourDC+BThreeDC+BTwoDC+BOneDC<=mullCount; BFiveDC++){
															for(int BSixDC =0; BSixDC+BFiveDC+BFourDC+BThreeDC+BTwoDC+BOneDC<=mullCount; BSixDC ++){
																int BLandDC = mullCount - BOneDC - BTwoDC - BThreeDC - BFourDC - BFiveDC - BSixDC;

																if(BOneDC<=OneDropCount && BTwoDC<=TwoDropCount && BThreeDC <=ThreeDropCount && BFourDC <=FourDropCount && BFiveDC <=FiveDropCount && BSixDC <=SixDropCount && BLandDC <= LandCount){
																	//
																	openinghand.SetHand(OneDropCount-BOneDC, TwoDropCount-BTwoDC, ThreeDropCount-BThreeDC, FourDropCount-BFourDC, FiveDropCount-BFiveDC, SixDropCount-BSixDC, LandCount-BLandDC);

																	deck.SetDeck(OriginalNr1Cost-BOneDC,OriginalNr2Cost-BTwoDC,OriginalNr3Cost-BThreeDC,OriginalNr4Cost-BFourDC,OriginalNr5Cost-BFiveDC,OriginalNr6Cost-BSixDC,OriginalNrLands-BLandDC);

																	double BotAvgManaSpent=AverageManaSpentForSpecificHand(deck,openinghand);

																	if(BotAvgManaSpent > highestBotAvgMana){
																		highestBotAvgMana = BotAvgManaSpent;
																		bestBOneDC = BOneDC;
																		bestBTwoDC = BTwoDC;
																		bestBThreeDC = BThreeDC;
																		bestBFourDC = BFourDC;
																		bestBFiveDC = BFiveDC;
																		bestBSixDC = BSixDC;
																		bestBLandDC = BLandDC;
																	}

																}

															}
														}

													}

												}

											}

										}



                                        //openinghand.SetHand(OneDropCount, TwoDropCount, ThreeDropCount, FourDropCount, FiveDropCount, SixDropCount, LandCount);
                                        //deck.SetDeck(OriginalNr1Cost,OriginalNr2Cost,OriginalNr3Cost,OriginalNr4Cost,OriginalNr5Cost,OriginalNr6Cost,OriginalNrLands);
                                        //double AvgManaSpent=AverageManaSpentForSpecificHand(deck,openinghand);
										//System.out.print(highestBotAvgMana + " : " + StartingCards + ":" + bestBOneDC+ " " +bestBTwoDC+ " " +bestBThreeDC+ " " +bestBFourDC+ " " +bestBFiveDC+ " " +bestBSixDC+ " " +bestBLandDC + "::" + OneDropCount+ " " +TwoDropCount+ " " +ThreeDropCount+ " " +FourDropCount+ " " +FiveDropCount+ " " +SixDropCount+ " " + LandCount);

										String logText = String.format("%.6f, %d, , %d, %d, %d, %d, %d, %d, %d, , %d, %d, %d, %d, %d, %d, %d,", highestBotAvgMana,StartingCards, OneDropCount, TwoDropCount, ThreeDropCount,FourDropCount, FiveDropCount, SixDropCount, LandCount, bestBOneDC,bestBTwoDC, bestBThreeDC,  bestBFourDC, bestBFiveDC, bestBSixDC, bestBLandDC);

										appendResultToFile("mulligan_strat.csv", logText);



                                        if (highestBotAvgMana>CutOffManaSpent) { 	KeepOpeningHand[StartingCards][OneDropCount][TwoDropCount][ThreeDropCount][FourDropCount][FiveDropCount][SixDropCount][LandCount]=new LondonMullChoice(true, bestBOneDC,bestBTwoDC,bestBThreeDC,bestBFourDC,bestBFiveDC,bestBSixDC,bestBLandDC);
											appendResultToFile("mulligan_strat.csv", "True\n");
										}

                                        if (highestBotAvgMana<=CutOffManaSpent) { KeepOpeningHand[StartingCards][OneDropCount][TwoDropCount][ThreeDropCount][FourDropCount][FiveDropCount][SixDropCount][LandCount]=new LondonMullChoice(false, bestBOneDC,bestBTwoDC,bestBThreeDC,bestBFourDC,bestBFiveDC,bestBSixDC,bestBLandDC);
											appendResultToFile("mulligan_strat.csv", "False\n");
										}

                                        if (StartingCards==2) {KeepOpeningHand[StartingCards][OneDropCount][TwoDropCount][ThreeDropCount][FourDropCount][FiveDropCount][SixDropCount][LandCount]=new LondonMullChoice(true, bestBOneDC,bestBTwoDC,bestBThreeDC,bestBFourDC,bestBFiveDC,bestBSixDC,bestBLandDC);}

										

                                    }
                                }
                            }
                        }
                    }
                }
            }
            deck.SetDeck(OriginalNr1Cost,OriginalNr2Cost,OriginalNr3Cost,OriginalNr4Cost,OriginalNr5Cost,OriginalNr6Cost,OriginalNrLands);
            if (StartingCards<7) {CutOffManaSpent=AverageManaSpentForRandomHand(deck,StartingCards,KeepOpeningHand,NumberOfSimulationsPerOpeningHandSize);}
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
        int NumberOfIterations=100000;
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
 
}//end of OptimalAggroGoldfishDeck
 
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


