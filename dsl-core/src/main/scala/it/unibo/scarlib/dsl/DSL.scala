package it.unibo.scarlib.dsl

import it.unibo.scarlib.core.deepRL.{CTDESystem, IndipendentAgent}
import it.unibo.scarlib.core.model.*

import scala.collection.mutable
import scala.collection.mutable.Seq as MSeq
import scala.reflect.runtime.universe as ru

object DSL {

    private var rf: Option[RewardFunction] = Option.empty
    private var env: Option[Environment] = Option.empty
    private var ds: Option[ReplayBuffer[State, Action]] = Option.empty
    private var actionSpace: Seq[Action] = Seq.empty
    private var nAgents: Int = 0

    def learningSystem(init: Unit ?=> Unit): CTDESystem =
        given unit: Unit = ()
        init
        var agentsSeq: Seq[IndipendentAgent] = Seq.empty
        for (n <- 0 to nAgents) {
            agentsSeq = agentsSeq :+ new IndipendentAgent(env.get, n, ds.get, actionSpace)
        }
        new CTDESystem(agentsSeq, ds.get, actionSpace, env.get)

    def environment(init: Unit ?=> String) =
        given unit: Unit = ()

        val name: String = init
        val runtimeMirror = ru.runtimeMirror(getClass.getClassLoader)
        val classSymbol = runtimeMirror.classSymbol(Class.forName(name))
        val classMirror = runtimeMirror.reflectClass(classSymbol)
        val constructor = classSymbol.typeSignature.members.filter(_.isConstructor).toList.head.asMethod
        val constructorMirror = classMirror.reflectConstructor(constructor).apply(rf.get, actionSpace)
        env = Option(constructorMirror.asInstanceOf[Environment])

    def rewardFunction(init: Unit ?=> RewardFunction) =
        given unit: Unit = ()
        rf = Option(init)

    def actions(init: Unit ?=> Seq[Action]) =
        given unit: Unit = ()
        actionSpace = init

    def dataset(init: Unit ?=> ReplayBuffer[State, Action]) =
        given unit: Unit = ()
        ds = Option(init)

    def agents(init: Unit ?=> Int) =
        given unit: Unit = ()
        nAgents = init



}
