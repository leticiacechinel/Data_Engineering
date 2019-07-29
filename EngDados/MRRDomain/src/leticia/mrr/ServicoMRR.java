package leticia.mrr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import leticia.cliente.Cliente;
import leticia.financeiro.Pagamento;
import leticia.infra.DAO;

public class ServicoMRR {

	private static SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

	public void pagamentoEfetuadoAsynch(int cliente, String data, String plano, double valor) throws ParseException {
	}

	public void pagamentoEfetuado(int cliente, String data, String plano, double valor) throws ParseException {
		DAO dao = new DAO();
		dao.begin();

		// Registra Pagamento
		Date dataPagamento = DF.parse(data);

		Pagamento pagamento = new Pagamento(cliente, dataPagamento, plano, valor);
		registraPagamento(dao, pagamento);

		dao.commit();

	}

	private void registraPagamento(DAO dao, Pagamento pagamento) {
		List<?> l = dao.get(Pagamento.class, "cliente = ?1 and dataPagamento = ?2", pagamento.getCliente(),
				pagamento.getDataPagamento());
		
		if (l.size() == 0) {
			dao.persist(pagamento);

			registraParcelas(pagamento.getCliente(), pagamento.getPlano(), pagamento.getValor(), dao,
					pagamento.getDataPagamento());

		}
	}

	private void registraParcelas(int clienteID, String plano, double valor, DAO dao, Date dataPagamento) {

		Cliente cliente = (Cliente) dao.find(Cliente.class, clienteID);

		// Registra Parcelas

		// quantas parcelas tem o plano
		int qtdParcelas = Integer.valueOf(plano.split("\\/")[1]);

		// valor de cada parcela
		double valorParcela = valor / qtdParcelas;

		// Trata New, Expansion, Contraction, Resurrected
		registraMudancaMRR(clienteID, dao, dataPagamento, valorParcela);

		Calendar c = Calendar.getInstance();
		c.setTime(dataPagamento);

		for (int i = 0; i < qtdParcelas; i++) {
			Parcela parcelaAtual = new Parcela(clienteID, dataPagamento, valorParcela);
			dao.persist(parcelaAtual);

			// metrica MRR

			MetricaCliente m = new MetricaCliente(clienteID, parcelaAtual.getMes(), Tipo.MRR, parcelaAtual.getValor());
			dao.persist(m);

			registraMetricaEmpresa(dao, parcelaAtual.getMes(), Tipo.MRR, parcelaAtual.getValor());
			registraMetricasAdicionais(dao, cliente, parcelaAtual.getMes(), Tipo.MRR, valorParcela);

			// proximo mês do plano
			c.add(Calendar.MONTH, 1);
			dataPagamento = c.getTime();

		}
	}

	private void registraMetricaEmpresa(DAO dao, int mes, Tipo tipo, double valor) {
		List<MetricaEmpresa> l = (List<MetricaEmpresa>) dao.get(MetricaEmpresa.class, "mes = ?1 and tipo = ?2", mes,
				tipo);

		MetricaEmpresa me;
		if (l.size() == 1) {
			me = l.get(0);
			me.add(valor);
		} else {
			me = new MetricaEmpresa(tipo, mes, valor);
		}

		dao.persist(me);
	}

	private void registraMudancaMRR(int clienteID, DAO dao, Date dataPagamento, double valorParcela) {
		boolean novo = false;

		Cliente cliente = (Cliente) dao.find(Cliente.class, clienteID);

		Parcela parcelaAtual = new Parcela(clienteID, dataPagamento, valorParcela);

		// registra new MRR
		if (dao.get(Parcela.class, "clienteID = ?1", clienteID).size() == 0) {
			MetricaCliente m = new MetricaCliente(clienteID, parcelaAtual.getMes(), Tipo.New_MRR, valorParcela);
			dao.persist(m);

			// registra métricas
			registraMetricaEmpresa(dao, parcelaAtual.getMes(), Tipo.New_MRR, valorParcela);

			registraMetricasAdicionais(dao, cliente, parcelaAtual.getMes(), Tipo.New_MRR, valorParcela);

			novo = true;
		}

		int mesAnterior = getMesAnterior(dataPagamento);

		List<Parcela> parcelas = (List<Parcela>) dao.get(Parcela.class, "clienteID = ?1 and mes = ?2", clienteID,
				mesAnterior);
		if (parcelas.size() == 1) { // comprou no mes anterior
			Parcela parcelaAnterior = parcelas.get(0);
			double delta = parcelaAtual.getValor() - parcelaAnterior.getValor();

			if (delta > 0) {
				/// Expansion MRR

				MetricaCliente m = new MetricaCliente(clienteID, parcelaAtual.getMes(), Tipo.Expansion_MRR, delta);
				dao.persist(m);

				registraMetricaEmpresa(dao, parcelaAtual.getMes(), Tipo.Expansion_MRR, delta);

				registraMetricasAdicionais(dao, cliente, parcelaAtual.getMes(), Tipo.Expansion_MRR, delta);

			} else if (delta < 0) {
				// Contraction MRR
				MetricaCliente m = new MetricaCliente(clienteID, parcelaAtual.getMes(), Tipo.Contraction_MRR,
						delta * -1);
				dao.persist(m);
				registraMetricaEmpresa(dao, parcelaAtual.getMes(), Tipo.Contraction_MRR, delta * -1);
				registraMetricasAdicionais(dao, cliente, parcelaAtual.getMes(), Tipo.Contraction_MRR, delta * -1);

			}
		} else { // sem compra mês Anterior
			if (!novo) {
				// Resurrected MRR
				MetricaCliente m = new MetricaCliente(clienteID, parcelaAtual.getMes(), Tipo.Resurrected_MRR,
						valorParcela);
				dao.persist(m);
				registraMetricaEmpresa(dao, parcelaAtual.getMes(), Tipo.Resurrected_MRR, valorParcela);
				registraMetricasAdicionais(dao, cliente, parcelaAtual.getMes(), Tipo.Resurrected_MRR, valorParcela);

			}
		}
	}

	private void registraMetricasAdicionais(DAO dao, Cliente cliente, int mes, Tipo tipo, double valor) {

		if (cliente == null) {
			return;
		}

		// registra métrica cidade
		registraMetricaCidade(dao, cliente.getCidade(), mes, tipo, valor);

		// registra métrica estado
		registraMetricaEstado(dao, cliente.getEstado(), mes, tipo, valor);

		// registra métrica segmento
		List<MetricaSegmento> ls = (List<MetricaSegmento>) dao.get(MetricaSegmento.class,
				"segmento = ?1and mes = ?2 and tipo = ?3 ", cliente.getSegmento(), mes, tipo);

		MetricaSegmento ms;
		if (ls.size() == 1) {
			ms = ls.get(0);
			ms.add(valor);
		} else {
			ms = new MetricaSegmento(cliente.getSegmento(), tipo, mes, valor);
		}
		dao.persist(ms);

	}

	private int getMesAnterior(Date data) {

		Calendar c = Calendar.getInstance();
		c.setTime(data);
		c.add(Calendar.MONTH, -1);

		int ano = c.get(Calendar.YEAR);
		int mes = c.get(Calendar.MONTH) + 1;

		return ano * 100 + mes;
	}

	private int getMesAnterior(int mesAtual) {

		int ano = mesAtual / 100;
		int mes = mesAtual % 100 - 1;

		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.MONTH, mes);
		c.set(Calendar.YEAR, ano);

		c.add(Calendar.MONTH, -1);

		ano = c.get(Calendar.YEAR);
		mes = c.get(Calendar.MONTH) + 1;

		return ano * 100 + mes;

	}

	public MetricaCliente[] getMetricas(int cliente, int mes) {
		DAO dao = new DAO();
		dao.begin();

		List<MetricaCliente> metricas = (List<MetricaCliente>) dao.get(MetricaCliente.class,
				"clienteID = ?1 and mes = ?2", cliente, mes);

		if (metricas.size() == 0) {
			int a = mes / 100;
			int m = mes % 100 - 1;
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.MONTH, m);
			c.set(Calendar.YEAR, a);

			int mesAnterior = getMesAnterior(c.getTime());

			List<Parcela> parcelas = (List<Parcela>) dao.get(Parcela.class, "clienteID = ?1 and mes = ?2", cliente,
					mesAnterior);

			if (parcelas.size() == 1) {
				Parcela parcelaAnterior = parcelas.get(0);

				// metrica Cancelled MRR

				MetricaCliente metrica = new MetricaCliente(cliente, mes, Tipo.Cancelled_MRR,
						parcelaAnterior.getValor());
				dao.persist(metrica);
				metricas = (List<MetricaCliente>) dao.get(MetricaCliente.class, "clienteID = ?1 and mes = ?2",
						cliente, mes);

			}
		}

		MetricaCliente[] ret = new MetricaCliente[metricas.size()];
		metricas.toArray(ret);
		dao.commit();

		return ret;

	}

	public MetricaEmpresa[] getMetricasEmpresa(int mes) {

		DAO dao = new DAO();
		dao.begin();

		List<?> l = dao.get(MetricaEmpresa.class, "mes = ?1", mes);

		if (l.size() < 6) {
			testaCriaMetricaEmpresa(dao, l, Tipo.MRR, mes);
			testaCriaMetricaEmpresa(dao, l, Tipo.Contraction_MRR, mes);
			testaCriaMetricaEmpresa(dao, l, Tipo.Expansion_MRR, mes);
			testaCriaMetricaEmpresa(dao, l, Tipo.Resurrected_MRR, mes);
			testaCriaMetricaEmpresa(dao, l, Tipo.New_MRR, mes);

			trataCancelledEmpresa(l, mes);

			l = dao.get(MetricaEmpresa.class, "mes = ?1", mes);
			testaCriaMetricaEmpresa(dao, l, Tipo.Cancelled_MRR, mes);

			l = dao.get(MetricaEmpresa.class, "mes = ?1", mes);

		}

		MetricaEmpresa[] ret = new MetricaEmpresa[l.size()];
		l.toArray(ret);
		dao.commit();

		return ret;

	}

	private void trataCancelledEmpresa(List<?> l, int mes) {

		DAO dao = new DAO();
		dao.begin();

		int mesAnt = getMesAnterior(mes);

		List<Parcela> parcelasMesPassado = (List<Parcela>) dao.get(Parcela.class, "mes = ?1", mesAnt);

		for (Parcela parcela : parcelasMesPassado) {

			// para cada parcela do mes passado verifica se o cliente tem
			// parcela no mes atual

			List<Parcela> parcelaMes = (List<Parcela>) dao.get(Parcela.class, "clienteID = ?1 and mes = ?2",
					parcela.getClienteID(), mes);

			if (parcelaMes.size() == 0) { // se não tem, é Cancelled
				registraMetricaEmpresa(dao, mes, Tipo.Cancelled_MRR, parcela.getValor());
			}
		}

		dao.commit();
	}

	private void trataCancelledCidade(List<?> l, String cidade, int mes) {

		DAO dao = new DAO();
		dao.begin();

		int mesAnt = getMesAnterior(mes);

		List<MetricaCidade> parcelasMesPassado = (List<MetricaCidade>) dao.get(MetricaCidade.class,
				"cidade = ?1 and mes = ?2 and tipo = ?3", cidade, mesAnt, Tipo.MRR);

		for (MetricaCidade parcela : parcelasMesPassado) {

			// para cada parcela do mes passado verifica se o cliente tem
			// parcela no mes atual

			List<MetricaCidade> parcelasMes = (List<MetricaCidade>) dao.get(MetricaCidade.class,
					"cidade = ?1 and mes = ?2 and tipo = ?3", cidade, mes, Tipo.MRR);

			if (parcelasMes.size() == 0) { // se não tem, é Cancelled
				registraMetricaCidade(dao, cidade, mes, Tipo.Cancelled_MRR, parcela.getValor());
			}
		}

		dao.commit();
	}

	private void registraMetricaCidade(DAO dao, String cidade, int mes, Tipo tipo, double valor) {
		List<MetricaCidade> l = (List<MetricaCidade>) dao.get(MetricaCidade.class,
				"cidade = ?1 and mes = ?2 and tipo = ?3 ", cidade, mes, tipo);

		MetricaCidade mc;
		if (l.size() == 1) {
			mc = l.get(0);
			mc.add(valor);
		} else {
			mc = new MetricaCidade(cidade, tipo, mes, valor);
		}
		dao.persist(mc);

	}

	private void testaCriaMetricaEmpresa(DAO dao, List<?> l, Tipo tipo, int mes) {
		MetricaEmpresa key = new MetricaEmpresa(tipo, mes);
		if (!l.contains(key)) {
			dao.persist(key);
		}
	}

	public MetricaCidade[] getMetricasCidade(String cidade, int mes) {

		DAO dao = new DAO();
		dao.begin();

		List<MetricaCidade> l = (List<MetricaCidade>) dao.get(MetricaCidade.class, "cidade = ?1 and mes = ?2",
				cidade, mes);

		if (l.size() < 6) {
			testaCriaMetricaCidade(dao, l, cidade, Tipo.MRR, mes);
			testaCriaMetricaCidade(dao, l, cidade, Tipo.Contraction_MRR, mes);
			testaCriaMetricaCidade(dao, l, cidade, Tipo.Expansion_MRR, mes);
			testaCriaMetricaCidade(dao, l, cidade, Tipo.Resurrected_MRR, mes);
			testaCriaMetricaCidade(dao, l, cidade, Tipo.New_MRR, mes);

			trataCancelledCidade(l, cidade, mes);

			l = (List<MetricaCidade>) dao.get(MetricaCidade.class, "cidade = ?1 and mes = ?2", cidade, mes);
			testaCriaMetricaCidade(dao, l, cidade, Tipo.Cancelled_MRR, mes);

			l = (List<MetricaCidade>) dao.get(MetricaCidade.class, "cidade = ?1 and mes = ?2", cidade, mes);

		}

		MetricaCidade[] ret = new MetricaCidade[l.size()];
		l.toArray(ret);
		dao.commit();

		return ret;

	}

	private void testaCriaMetricaCidade(DAO dao, List<?> l, String cidade, Tipo tipo, int mes) {
		MetricaCidade key = new MetricaCidade(cidade, tipo, mes);
		if (!l.contains(key)) {
			dao.persist(key);
		}

	}

	public MetricaEstado[] getMetricasEstado(String estado, int mes) {

		DAO dao = new DAO();
		dao.begin();

		List<MetricaEstado> l = (List<MetricaEstado>) dao.get(MetricaEstado.class, "estado = ?1 and mes = ?2",
				estado, mes);

		if (l.size() < 6) {
			testaCriaMetricaEstado(dao, l, estado, Tipo.MRR, mes);
			testaCriaMetricaEstado(dao, l, estado, Tipo.Contraction_MRR, mes);
			testaCriaMetricaEstado(dao, l, estado, Tipo.Expansion_MRR, mes);
			testaCriaMetricaEstado(dao, l, estado, Tipo.Resurrected_MRR, mes);
			testaCriaMetricaEstado(dao, l, estado, Tipo.New_MRR, mes);

			trataCancelledEstado(l, estado, mes);

			l = (List<MetricaEstado>) dao.get(MetricaEstado.class, "estado = ?1 and mes = ?2", estado, mes);
			testaCriaMetricaEstado(dao, l, estado, Tipo.Cancelled_MRR, mes);

			l = (List<MetricaEstado>) dao.get(MetricaEstado.class, "estado = ?1 and mes = ?2", estado, mes);

		}

		MetricaEstado[] ret = new MetricaEstado[l.size()];
		l.toArray(ret);
		dao.commit();

		return ret;

	}

	private void trataCancelledEstado(List<MetricaEstado> l, String estado, int mes) {
		DAO dao = new DAO();
		dao.begin();

		int mesAnt = getMesAnterior(mes);

		List<MetricaEstado> parcelasMesPassado = (List<MetricaEstado>) dao.get(MetricaEstado.class,
				"estado = ?1 and mes = ?2 and tipo = ?3", estado, mesAnt, Tipo.MRR);

		for (MetricaEstado parcela : parcelasMesPassado) {

			// para cada parcela do mes passado verifica se o cliente tem
			// parcela no mes atual

			List<MetricaEstado> parcelasMes = (List<MetricaEstado>) dao.get(MetricaEstado.class,
					"estado = ?1 and mes = ?2 and tipo = ?3", estado, mes, Tipo.MRR);

			if (parcelasMes.size() == 0) { // se não tem, é Cancelled
				registraMetricaEstado(dao, estado, mes, Tipo.Cancelled_MRR, parcela.getValor());
			}
		}

		dao.commit();
	}

	private void registraMetricaEstado(DAO dao, String estado, int mes, Tipo tipo, double valor) {
		List<MetricaEstado> l = (List<MetricaEstado>) dao.get(MetricaEstado.class,
				"estado = ?1 and mes = ?2 and tipo = ?3", estado, mes, tipo);

		MetricaEstado mc;
		if (l.size() == 1) {
			mc = l.get(0);
			mc.add(valor);
		} else {
			mc = new MetricaEstado(estado, tipo, mes, valor);
		}
		dao.persist(mc);

	}

	private void testaCriaMetricaEstado(DAO dao, List<MetricaEstado> l, String estado, Tipo tipo, int mes) {
		MetricaEstado key = new MetricaEstado(estado, tipo, mes);
		if (!l.contains(key)) {
			dao.persist(key);
		}
	}

	public MetricaSegmento[] getMetricasSegmento(String segmento, int mes) {
		DAO dao = new DAO();
		dao.begin();

		List<MetricaSegmento> l = (List<MetricaSegmento>) dao.get(MetricaSegmento.class,
				"segmento = ?1 and mes = ?2", segmento, mes);

		if (l.size() < 6) {
			testaCriaMetricaSegmento(dao, l, segmento, Tipo.MRR, mes);
			testaCriaMetricaSegmento(dao, l, segmento, Tipo.Contraction_MRR, mes);
			testaCriaMetricaSegmento(dao, l, segmento, Tipo.Expansion_MRR, mes);
			testaCriaMetricaSegmento(dao, l, segmento, Tipo.Resurrected_MRR, mes);
			testaCriaMetricaSegmento(dao, l, segmento, Tipo.New_MRR, mes);

			trataCancelledSegmento(l, segmento, mes);

			l = (List<MetricaSegmento>) dao.get(MetricaSegmento.class, "segmento = ?1 and mes = ?2", segmento, mes);
			testaCriaMetricaSegmento(dao, l, segmento, Tipo.Cancelled_MRR, mes);

			l = (List<MetricaSegmento>) dao.get(MetricaSegmento.class, "segmento = ?1 and mes = ?2", segmento, mes);

		}

		MetricaSegmento[] ret = new MetricaSegmento[l.size()];
		l.toArray(ret);
		dao.commit();

		return ret;
	}

	private void trataCancelledSegmento(List<MetricaSegmento> l, String segmento, int mes) {
		// TODO Auto-generated method stub

	}

	private void testaCriaMetricaSegmento(DAO dao, List<MetricaSegmento> l, String segmento, Tipo tipo, int mes) {
		MetricaSegmento key = new MetricaSegmento(segmento, tipo, mes);
		if (!l.contains(key)) {
			dao.persist(key);
		}

	}

	public void uploadClientes(Cliente[] clientes) {
		DAO dao = new DAO();
		dao.begin();

		int i = 0;
		for (Cliente cliente : clientes) {
			dao.merge(cliente);

			if (++i % 100 == 0) {
				System.out.println(i);
				dao.commit();
				dao.begin();
			}
		}

		dao.commit();

	}

	public void uploadPagamentos(List<Pagamento> pagamentos) {
		DAO dao = new DAO();
		dao.begin();

		int i = 0;
		for (Pagamento pagamento : pagamentos) {
			registraPagamento(dao, pagamento);

			if (++i % 100 == 0) {
				System.out.println(i);
				dao.commit();
				dao.begin();
			}
		}

		dao.commit();

	}

}
