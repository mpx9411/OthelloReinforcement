# OthelloReinforcement

Magnus Palmstierna, Paulina Kekkonen, Ida Söderberg

grupp 5

GitHub: https://github.com/mpx9411/OthelloReinforcement/tree/magnus 

I denna uppgift har gruppen utarbetat en tidigare version av Othello. Den tidigare versionen använde sig utav minimax, som motspelare mot en riktig person. I denna version spelar en Q-learning agent mot antingen en minimaxbot eller en bot som gör slumpvalda dragningar. 
Q-learning agenten använder sig på ett antal vikter för att sätta belöningen på ett möjligt drag. Agenten tränas genom att n av dess vikter justeras. Sedan körs tio matcher, med q-learning agenten på en sida och antingen en minimaxbot eller en randomized bot på andra sidan. Om Q-learning agenten vinner minst hälften av matcherna så sparas den nya vikten till filen weights. Annars behålls den gamla vikten. 
Brister i implementationen:
- q-learning lär sig om den vinner mer än fem matcher, men inte om den förlorar
- viktjusteringarna kan vara så små att den fastnat med icke-optimala vikter för att den aldrig belönas för justeringarna 

