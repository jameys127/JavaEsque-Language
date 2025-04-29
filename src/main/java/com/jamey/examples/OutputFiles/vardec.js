class Animal {
constructor() {
}
speak() {
return;
}
}
class Cat extends Animal {
constructor(y) {
super();
this.other = y;
}
meow(x, y) {
return console.log(1);
}
}
class Mouse extends Cat {
constructor(x, y) {
super(x);
this.something = y;
}
meow(d, f) {
let c;
c = (f + d);
console.log(c);
}
squeak(x, y) {
return console.log(2);
}
gettingStuff() {
return this.other;
}
}
let animal;
let cat;
let mouse;
animal = new Animal();
cat = new Cat(7);
mouse = new Mouse(36, 12);
cat.speak();
mouse.speak();
mouse.meow(5, 6);
mouse.squeak(1, 4);
console.log(mouse.gettingStuff());
