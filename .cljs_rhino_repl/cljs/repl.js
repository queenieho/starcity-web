// Compiled by ClojureScript 1.7.170 {}
goog.provide('cljs.repl');
goog.require('cljs.core');
cljs.repl.print_doc = (function cljs$repl$print_doc(m){
cljs.core.println.call(null,"-------------------------");

cljs.core.println.call(null,[cljs.core.str((function (){var temp__4657__auto__ = new cljs.core.Keyword(null,"ns","ns",441598760).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(temp__4657__auto__)){
var ns = temp__4657__auto__;
return [cljs.core.str(ns),cljs.core.str("/")].join('');
} else {
return null;
}
})()),cljs.core.str(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Protocol");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m))){
var seq__45579_45593 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m));
var chunk__45580_45594 = null;
var count__45581_45595 = (0);
var i__45582_45596 = (0);
while(true){
if((i__45582_45596 < count__45581_45595)){
var f_45597 = cljs.core._nth.call(null,chunk__45580_45594,i__45582_45596);
cljs.core.println.call(null,"  ",f_45597);

var G__45598 = seq__45579_45593;
var G__45599 = chunk__45580_45594;
var G__45600 = count__45581_45595;
var G__45601 = (i__45582_45596 + (1));
seq__45579_45593 = G__45598;
chunk__45580_45594 = G__45599;
count__45581_45595 = G__45600;
i__45582_45596 = G__45601;
continue;
} else {
var temp__4657__auto___45602 = cljs.core.seq.call(null,seq__45579_45593);
if(temp__4657__auto___45602){
var seq__45579_45603__$1 = temp__4657__auto___45602;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__45579_45603__$1)){
var c__45226__auto___45604 = cljs.core.chunk_first.call(null,seq__45579_45603__$1);
var G__45605 = cljs.core.chunk_rest.call(null,seq__45579_45603__$1);
var G__45606 = c__45226__auto___45604;
var G__45607 = cljs.core.count.call(null,c__45226__auto___45604);
var G__45608 = (0);
seq__45579_45593 = G__45605;
chunk__45580_45594 = G__45606;
count__45581_45595 = G__45607;
i__45582_45596 = G__45608;
continue;
} else {
var f_45609 = cljs.core.first.call(null,seq__45579_45603__$1);
cljs.core.println.call(null,"  ",f_45609);

var G__45610 = cljs.core.next.call(null,seq__45579_45603__$1);
var G__45611 = null;
var G__45612 = (0);
var G__45613 = (0);
seq__45579_45593 = G__45610;
chunk__45580_45594 = G__45611;
count__45581_45595 = G__45612;
i__45582_45596 = G__45613;
continue;
}
} else {
}
}
break;
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m))){
var arglists_45614 = new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_((function (){var or__44423__auto__ = new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(or__44423__auto__)){
return or__44423__auto__;
} else {
return new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m);
}
})())){
cljs.core.prn.call(null,arglists_45614);
} else {
cljs.core.prn.call(null,((cljs.core._EQ_.call(null,new cljs.core.Symbol(null,"quote","quote",1377916282,null),cljs.core.first.call(null,arglists_45614)))?cljs.core.second.call(null,arglists_45614):arglists_45614));
}
} else {
}
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"special-form","special-form",-1326536374).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Special Form");

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.contains_QMARK_.call(null,m,new cljs.core.Keyword(null,"url","url",276297046))){
if(cljs.core.truth_(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))){
return cljs.core.println.call(null,[cljs.core.str("\n  Please see http://clojure.org/"),cljs.core.str(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))].join(''));
} else {
return null;
}
} else {
return cljs.core.println.call(null,[cljs.core.str("\n  Please see http://clojure.org/special_forms#"),cljs.core.str(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Macro");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"REPL Special Function");
} else {
}

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
var seq__45583 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"methods","methods",453930866).cljs$core$IFn$_invoke$arity$1(m));
var chunk__45584 = null;
var count__45585 = (0);
var i__45586 = (0);
while(true){
if((i__45586 < count__45585)){
var vec__45587 = cljs.core._nth.call(null,chunk__45584,i__45586);
var name = cljs.core.nth.call(null,vec__45587,(0),null);
var map__45588 = cljs.core.nth.call(null,vec__45587,(1),null);
var map__45588__$1 = ((((!((map__45588 == null)))?((((map__45588.cljs$lang$protocol_mask$partition0$ & (64))) || (map__45588.cljs$core$ISeq$))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__45588):map__45588);
var doc = cljs.core.get.call(null,map__45588__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists = cljs.core.get.call(null,map__45588__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name);

cljs.core.println.call(null," ",arglists);

if(cljs.core.truth_(doc)){
cljs.core.println.call(null," ",doc);
} else {
}

var G__45615 = seq__45583;
var G__45616 = chunk__45584;
var G__45617 = count__45585;
var G__45618 = (i__45586 + (1));
seq__45583 = G__45615;
chunk__45584 = G__45616;
count__45585 = G__45617;
i__45586 = G__45618;
continue;
} else {
var temp__4657__auto__ = cljs.core.seq.call(null,seq__45583);
if(temp__4657__auto__){
var seq__45583__$1 = temp__4657__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__45583__$1)){
var c__45226__auto__ = cljs.core.chunk_first.call(null,seq__45583__$1);
var G__45619 = cljs.core.chunk_rest.call(null,seq__45583__$1);
var G__45620 = c__45226__auto__;
var G__45621 = cljs.core.count.call(null,c__45226__auto__);
var G__45622 = (0);
seq__45583 = G__45619;
chunk__45584 = G__45620;
count__45585 = G__45621;
i__45586 = G__45622;
continue;
} else {
var vec__45590 = cljs.core.first.call(null,seq__45583__$1);
var name = cljs.core.nth.call(null,vec__45590,(0),null);
var map__45591 = cljs.core.nth.call(null,vec__45590,(1),null);
var map__45591__$1 = ((((!((map__45591 == null)))?((((map__45591.cljs$lang$protocol_mask$partition0$ & (64))) || (map__45591.cljs$core$ISeq$))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__45591):map__45591);
var doc = cljs.core.get.call(null,map__45591__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists = cljs.core.get.call(null,map__45591__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name);

cljs.core.println.call(null," ",arglists);

if(cljs.core.truth_(doc)){
cljs.core.println.call(null," ",doc);
} else {
}

var G__45623 = cljs.core.next.call(null,seq__45583__$1);
var G__45624 = null;
var G__45625 = (0);
var G__45626 = (0);
seq__45583 = G__45623;
chunk__45584 = G__45624;
count__45585 = G__45625;
i__45586 = G__45626;
continue;
}
} else {
return null;
}
}
break;
}
} else {
return null;
}
}
});

//# sourceMappingURL=repl.js.map