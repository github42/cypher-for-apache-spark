package org.opencypher.spark.benchmark

object BenchmarkSeries {
  def run[G](benchmarkAndGraph: BenchmarkAndGraph[G], nbrTimes: Int = 7, warmupTimes: Int = 3): BenchmarkResult = {
    val (planTime, plan) = warmup(benchmarkAndGraph, warmupTimes)
    measure(benchmarkAndGraph, nbrTimes, planTime, plan)
  }

  private def planAndTime(f: => String): (Long, String) = {
    println(s"Timing -- Plan")
    val start = System.currentTimeMillis()
    val plan = f
    val time = System.currentTimeMillis() - start
    println(s"Done -- $time ms")
    time -> plan
  }

  private def runAndTime(i: Int, f: => Outcome): (Long, Outcome) = {
    println(s"Timing -- Run $i")
    val start = System.currentTimeMillis()
    val outcome = f
    val time = System.currentTimeMillis() - start
    println(s"Done -- $time ms")
    time -> outcome
  }

  private def warmup[G](benchmarkAndGraph: BenchmarkAndGraph[G], nbrTimes: Int): (Long, String) = {
    benchmarkAndGraph.use { (benchmark, graph) =>
      benchmark.init(graph)
      val planInfo = planAndTime(benchmark.plan(graph))
      println("Begin warmup")
      (0 until nbrTimes).foreach { i =>
        runAndTime(i, benchmark.run(graph))
      }
      planInfo
    }
  }

  private def measure[G](benchmarkAndGraph: BenchmarkAndGraph[G], nbrTimes: Int, planTime: Long, plan: String) = {
    benchmarkAndGraph.use { (benchmark, graph) =>
      val initialOutcome = benchmark.run(graph)
      val count = initialOutcome.computeCount
      val checksum = initialOutcome.computeChecksum

      println("Begin measurements")
      val outcomes = (0 until nbrTimes).map { i =>
        val (time, outcome) = runAndTime(i, benchmark.run(graph))
        if (outcome.usedCachedPlan) time + planTime else time
      }
      BenchmarkResult(benchmark.name, outcomes, plan, count, checksum)
    }
  }
}