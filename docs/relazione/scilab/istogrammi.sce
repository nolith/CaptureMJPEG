// Istogramma prestazioni
//clf();

//dati di raffronto:
//
//parametri:
//algoritmo blur
//
//dimensione 320x240
//frame rate 20
//utilizzo CPU: 60% capturemjpeg in remoto su micc, con utilizzo memoria 30mb reali
//	      40% classe capture qt4java, con utilizzo di memoria 40mb reali
//
//dimensione 640x480
//frame rate 20
//utilizzo CPU: 85% capturemjpeg in remoto su micc, con utilizzo memoria 50mb reali
//	      90% classe capture qt4java, con utilizzo di memoria 60mb reali
//
//
//con dimensione 320x240
//
//frameRate (10)
//40% capturemjpeg
//35% qt4java
//
//frameRate (5)
//30% capturemjpeg
//20% qt4java
//
//con dimensione 640x480
//framerate (5)
//60% capturemjpeg
//65% qt4java
//
//framerate (10)
//85% capturemjpeg
//90% qt4java
//
//
//algoritmo histogram
//dimensione 320x240
//frame rate 20
//utilizzo CPU: 80% capturemjpeg in remoto su micc, con utilizzo memoria 30mb reali
//	      20% classe capture qt4java, con utilizzo di memoria 40mb reali
//
//dimensione 640x480
//frame rate 20
//utilizzo CPU: 90% capturemjpeg in remoto su micc, con utilizzo memori 40mb reali
//	      70% classe capture qt4java, con utilizzo di memoria 50mb reali
//
//con dimensione 640x480
//frameRate (10)
//90% capturemjpeg
//60% qt4java
//
//frameRate (5)
//80% capturemjpeg
//35% qt4java
//
//
clf(1); scf(1);
xtitle('Analisi blur 640x480','FPS', 'Utilizzo CPU %')
x=['5' '10' '20']'; y=[60 65;85 90;85 90];

bar(y);
a=gca();
a.data_bounds(4)=100;
a.x_ticks.labels = x;
legend(['CaptureMJPEG';'Capture'],4);
xs2eps(1,'isto_blur_640')

clf(2); scf(2);
xtitle('Analisi blur 320x240','FPS', 'Utilizzo CPU %')
y=[30 20;40 35;60 40];

bar(y);
a=gca();
a.data_bounds(4)=100;
a.x_ticks.labels = x;
legend(['CaptureMJPEG';'Capture'],4);
xs2eps(2,'isto_blur_320')

clf(3); scf(3);
xtitle('Analisi histogram 640x480','FPS', 'Utilizzo CPU %')
y=[80 35;90 60;90 70];

bar(y);
a=gca();
a.data_bounds(4)=100;
a.x_ticks.labels = x;
legend(['CaptureMJPEG';'Capture'],4);
xs2eps(3,'isto_histo_640')

