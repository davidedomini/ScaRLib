package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.{Action, Environment, RewardFunction, State}
import it.unibo.scarlib.core.util.{AgentGlobalStore, Logger}

import scala.concurrent.Future
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.PyQuote

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class VmasEnvironment(rewardFunction: RewardFunction,
                      actionSpace: Seq[Action], settings: VmasSettings, logger: Logger)
  extends Environment(rewardFunction, actionSpace) {


    private val VMAS: py.Module = py.module("vmas")
    private val env:py.Dynamic = VMAS.make_env(
        scenario=settings.scenario,
        num_envs=settings.nEnv,
        device=settings.device,
        continuos_actions=settings.continuousActions,
        dict_spaces=settings.dictionarySpaces,
        n_agents=settings.nAgents,
        n_targets=settings.nTargets
    )

    private var lastObservation: Option[VMASState] = Option.empty
    private var ticks = 0

    private var actions: Seq[py.Dynamic] = Seq[py.Dynamic]()
    private var futures = Seq[Future[(Double, State)]]()

    /** A single interaction with an agent
     *
     * @param action  the action performed by the agent
     * @param agentId the agent unique id
     * @return a [[Future]] that contains the reward of the action and the next state
     */
    override def step(action: Action, agentId: Int): Future[(Double, State)] = {
        //Check if agent is the last one
        val agents = env.agents.as[mutable.Seq[py.Dynamic]]
        //val agentPos = agents(agentId).pos //Tensor of shape [n_env, 2] - NOT USED
        actions = actions :+ action.asInstanceOf[VMASAction].toTensor()
        val nAgents:Int = env.n_agents.as[Int]
        val isLast = nAgents-1 == agentId
        val promise = scala.concurrent.Promise[(Double, State)]()
        val future = promise.future
        futures = futures :+ future
        if (isLast){
            val result = env.step(actions.toPythonCopy)
            val observations = result.bracketAccess(0)
            val rewards = result.bracketAccess(1)
            for (i <- 0 until nAgents ) {
                val reward = rewards.bracketAccess(i).as[Double]
                val observation = observations.bracketAccess(i)
                val state = VMASState(observation)
                lastObservation = Some(state) //TODO check if this is correct
                promise.success((reward, state))
            }
        }
        return future
    }

    /** Gets the current state of the environment */
    override def observe(agentId: Int): State = lastObservation match{
        case Some(obs) => obs
        case None => {
            lastObservation = Some(VMASState(env.get_observation_space()))
            lastObservation.get
        }
    }

    /** Resets the environment to the initial state */
    override def reset(): Unit = {
        env.reset()
        lastObservation = None
    }

    override def log(): Unit = {
        AgentGlobalStore.sumAllNumeric(AgentGlobalStore()).foreach { case (k, v) =>
            logger.logScalar(k, v, ticks)
        }
        AgentGlobalStore().clearAll()
    }

    def logOnFile(): Unit = ???
}
