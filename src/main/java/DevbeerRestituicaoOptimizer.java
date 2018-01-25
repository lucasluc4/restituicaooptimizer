import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

import java.util.HashMap;
import java.util.Map;

public class DevbeerRestituicaoOptimizer {

    private static final double[] baseCalculo = criarBasesCalculo();

    private static double[] criarBasesCalculo() {
        double[] bases = new double[12];
        bases[0] = 100;
        bases[1] = 60;
        bases[2] = 40;
        bases[3] = 10;
        bases[4] = 250;
        bases[5] = -150;
        bases[6] = -20;
        bases[7] = -50;
        bases[8] = 200;
        bases[9] = 120;
        bases[10] = -210;
        bases[11] = -15;

        return bases;
    }

    private static double fitnessFunction(Genotype<IntegerGene> gt) {
        Map<Integer, Double> map = new HashMap<>();
        int[] integerList = gt.getChromosome().as(IntegerChromosome.class).toArray();
        int index = 0;
        for (int i : integerList) {
            Double mappedDouble = map.get(i);
            if (mappedDouble == null) {
                map.put(i, baseCalculo[index]);
            } else {
                map.put(i, mappedDouble + baseCalculo[index]);
            }
            index++;
        }

        double resultado = 0;
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {
            if (entry.getValue() > 0) {
                resultado += entry.getValue();
            }
        }

        return resultado;
    }

    public static void main(String[] args) {
        Factory<Genotype<IntegerGene>> genotypeFactory = Genotype.of(IntegerChromosome.of(1, 12, 12));

        Engine<IntegerGene, Double> engine = Engine
                .builder(DevbeerRestituicaoOptimizer::fitnessFunction, genotypeFactory)
                .optimize(Optimize.MINIMUM)
                .populationSize(300)
                .survivorsSelector(new TournamentSelector<>(2))
                .offspringSelector(new RouletteWheelSelector<>())
                .alterers(
                        new SinglePointCrossover<>(0.2),
                        new Mutator<>(0.2))
                .build();

        Genotype<IntegerGene> result = engine.stream()
                .limit(300)
                .collect(EvolutionResult.toBestGenotype());

        // 335
        System.out.println("Resultado: " + fitnessFunction(result));
    }
}
