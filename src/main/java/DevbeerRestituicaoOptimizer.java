import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

import java.util.HashMap;
import java.util.Map;

public class DevbeerRestituicaoOptimizer {

    private static final double[] baseCalculo = criarBasesCalculo();
    private static final int[][] restricoesPorIndividuo = criarRestricoes();

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

    private static int[][] criarRestricoes() {

        int[][] restricoesPorIndividuo = new int[12][];
        restricoesPorIndividuo[0] = new int[0];
        restricoesPorIndividuo[1] = new int[0];
        restricoesPorIndividuo[2] = new int[0];

        restricoesPorIndividuo[3] = new int[2];
        restricoesPorIndividuo[3][0] = 0;
        restricoesPorIndividuo[3][1] = 9;

        restricoesPorIndividuo[4] = new int[0];
        restricoesPorIndividuo[5] = new int[0];
        restricoesPorIndividuo[6] = new int[0];
        restricoesPorIndividuo[7] = new int[0];
        restricoesPorIndividuo[8] = new int[0];
        restricoesPorIndividuo[9] = new int[0];

        restricoesPorIndividuo[10] = new int[2];
        restricoesPorIndividuo[10][0] = 4;
        restricoesPorIndividuo[10][1] = 8;


        restricoesPorIndividuo[11] = new int[1];
        restricoesPorIndividuo[11][0] = 9;

        return restricoesPorIndividuo;
    }

    private static double fitnessFunction(Genotype<IntegerGene> gt) {
        Map<Integer, Double> baseDeCalculoAcumuladaPorGrupo = new HashMap<>();
        Map<Integer, Integer> titularMap = new HashMap<>();

        int[] agrupamentoList = gt.getChromosome().as(IntegerChromosome.class).toArray();
        int individuo = 0;
        for (int i : agrupamentoList) {
            Double mappedDouble = baseDeCalculoAcumuladaPorGrupo.get(i);
            if (mappedDouble == null) {
                baseDeCalculoAcumuladaPorGrupo.put(i, baseCalculo[individuo]);
                titularMap.put(i, individuo);
            } else {
                baseDeCalculoAcumuladaPorGrupo.put(i, mappedDouble + baseCalculo[individuo]);
                if (baseCalculo[titularMap.get(i)] < baseCalculo[individuo]) {
                    titularMap.put(i, individuo);
                }
            }
            individuo++;
        }

        if (!valido(agrupamentoList, titularMap)) {
            return 10000;
        }

        double resultado = 0;
        for (Map.Entry<Integer, Double> entry : baseDeCalculoAcumuladaPorGrupo.entrySet()) {
            if (entry.getValue() > 0) {
                resultado += entry.getValue();
            }
        }

        return resultado/((double) baseDeCalculoAcumuladaPorGrupo.entrySet().size());
    }

    private static boolean valido(int[] agrupamentoList, Map<Integer, Integer> titularMap) {

        for (int individuoAtual = 0; individuoAtual < baseCalculo.length; individuoAtual++) {
            int grupoIndividuo = agrupamentoList[individuoAtual];

            if (titularMap.get(grupoIndividuo) == individuoAtual || restricoesPorIndividuo[individuoAtual].length == 0) {
                continue;
            }

            boolean obedeceuRestricao = false;
            for (int restricao : restricoesPorIndividuo[individuoAtual]) {
                if (agrupamentoList[restricao] == grupoIndividuo) {
                    obedeceuRestricao = true;
                }
            }

            if (obedeceuRestricao) {
                continue;
            }

            return false;
        }

        return true;
    }

    private static double avaliarTotalBaseDeCalculo (int[] agrupamento) {
        Map<Integer, Double> baseDeCalculoAcumuladaPorGrupo = new HashMap<>();
        int individuo = 0;
        for (int i : agrupamento) {
            Double mappedDouble = baseDeCalculoAcumuladaPorGrupo.get(i);
            if (mappedDouble == null) {
                baseDeCalculoAcumuladaPorGrupo.put(i, baseCalculo[individuo]);
            } else {
                baseDeCalculoAcumuladaPorGrupo.put(i, mappedDouble + baseCalculo[individuo]);
            }
            individuo++;
        }

        double resultado = 0;
        for (Map.Entry<Integer, Double> entry : baseDeCalculoAcumuladaPorGrupo.entrySet()) {
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
        double totalBaseDeCalculoAcumulada =
                avaliarTotalBaseDeCalculo(result.getChromosome().as(IntegerChromosome.class).toArray());
        double score = fitnessFunction(result);

        System.out.println("Resultado de Base de Calculo Acumulada: " + totalBaseDeCalculoAcumulada);
        System.out.println("Quantidade de Grupos: " + (totalBaseDeCalculoAcumulada/score));

        StringBuilder stringBuilder = new StringBuilder("Agrupamento Resultante: ");
        int[] agrupamento = result.getChromosome().as(IntegerChromosome.class).toArray();
        for (int grupoIndividuo : agrupamento) {
            stringBuilder.append(grupoIndividuo);
            stringBuilder.append("|");
        }
        System.out.println(stringBuilder.toString());
    }
}
