class Animal {
constructor() {
}
speak() {
return;
}
}
class Cat extends Animal {
constructor() {
super();
}
speak() {
console.log("meow");
}
meow(x, y) {
return console.log(1);
}
}
class Mouse extends Cat {
constructor() {
super();
}
speak() {
console.log("squeek");
}
meow(d, f) {
let c;
c = (d + f);
console.log(c);
}
squeak(x, y) {
return console.log(2);
}
}
let animal;
let cat;
let mouse;
animal = new Animal();
cat = new Cat();
mouse = new Mouse();
cat.speak();
mouse.speak();
mouse.meow(5, 6);
