package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.MyState
import it.unibo.scarlib.core.model.{Action, Decay, DeepQLearner, ExponentialDecay, GeneralEnvironment, ReplayBuffer, State}
import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding

import scala.collection.immutable.Seq
import scala.reflect.io.File
import scala.util.Random

class DecentralizedAgent(
                          agentId: Int,
                          environment: GeneralEnvironment,
                          datasetSize: Int,
                          actionSpace: Seq[Action]):

    private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)
    private val epsilon: Decay[Double] = ExponentialDecay(0.9, 0.1, 0.01)
    private val learner: DeepQLearner = DeepQLearner(dataset, actionSpace, epsilon, 0.9, 0.0005, inputSize = 4)(Random(42)) //TODO migliora inputsize
    private val posLogs: StringBuilder = StringBuilder()

    def step: Unit =
        val state = environment.observe(agentId)
        val policy = learner.behavioural
        val action = policy(state)
        val result: (Double, State) = environment.step(action, agentId)
        logPos(result._2.asInstanceOf[MyState].agentPosition)
        dataset.insert(state, action, result._1, result._2)
        learner.improve()
        epsilon.update
  
    private def logPos(pos: (Double, Double)): Unit =
        posLogs.append(pos.toString + "\n")

    def logOnFile: Unit =
        val file = File(s"agent-${agentId}.txt")
        val bw = file.bufferedWriter(append = true)
        bw.write(posLogs.toString())
        bw.close()