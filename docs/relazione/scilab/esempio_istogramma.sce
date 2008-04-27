// Istogramma prestazioni
//clf();
scf(0);
xtitle('Analisi video 640x480','FPS', 'CPU usage %')
x=['8' '10' '20']'; y=[60 70;69 45;80 90];

bar(y);
a=gca();
a.data_bounds(4)=100;
a.x_ticks.labels = x;
legend(['CaptureMJPEG';'Capture'],4);
xs2eps(0,'istogramma')
