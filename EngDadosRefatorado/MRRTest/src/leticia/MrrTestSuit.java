package leticia;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import leticia.financeiro.IntegracaoPagamentoTest;
import leticia.financeiro.PagamentoTest;
import leticia.mrr.CidadeEstadoSegmentoTest;
import leticia.mrr.MetricasClientesTest;
import leticia.mrr.MetricasGeraisTest;

@RunWith(Suite.class)
@SuiteClasses({CidadeEstadoSegmentoTest.class, IntegracaoPagamentoTest.class, PagamentoTest.class,MetricasClientesTest.class,MetricasGeraisTest.class})
public class MrrTestSuit {
	

}
