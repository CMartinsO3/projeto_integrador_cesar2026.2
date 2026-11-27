"""Gera docs/arquitetura/orcamento/orcamento-cenarios.xlsx (orçamento dos 3 cenários).

Execute: python docs/arquitetura/fontes/gerar_planilha.py
Preços: estimativas de preço de lista (us-east-1 / planos públicos), referência nov/2026.
Confira nas calculadoras oficiais antes de qualquer contratação.
"""
from pathlib import Path

from openpyxl import Workbook
from openpyxl.chart import BarChart, Reference
from openpyxl.comments import Comment
from openpyxl.styles import Alignment, Border, Font, PatternFill, Side

DESTINO = Path(__file__).resolve().parents[1] / "orcamento" / "orcamento-cenarios.xlsx"

FONTE = "Arial"
AZUL = Font(name=FONTE, color="0000FF")
PRETO = Font(name=FONTE)
VERDE = Font(name=FONTE, color="008000")
NEGRITO = Font(name=FONTE, bold=True)
TITULO = Font(name=FONTE, bold=True, size=14, color="B3121F")
CAB = Font(name=FONTE, bold=True, color="FFFFFF")
FUNDO_CAB = PatternFill("solid", fgColor="7D0C15")
AMARELO = PatternFill("solid", fgColor="FFFF00")
CINZA = PatternFill("solid", fgColor="F3F3F3")
FINA = Side(style="thin", color="D0C6C4")
BORDA = Border(left=FINA, right=FINA, top=FINA, bottom=FINA)
USD = '"US$" #,##0.00;("US$" #,##0.00);"-"'
BRL = '"R$" #,##0.00;("R$" #,##0.00);"-"'
BRL0 = '"R$" #,##0;("R$" #,##0);"-"'
HORAS = '#,##0" h";;"-"'

wb = Workbook()

# ------------------------------------------------------------------ Premissas
p = wb.active
p.title = "Premissas"
p["A1"] = "HemoFlow — Orçamento dos cenários de arquitetura"; p["A1"].font = TITULO
p["A2"] = "Células em azul com fundo amarelo são entradas: altere-as e toda a planilha recalcula."
p["A2"].font = Font(name=FONTE, italic=True, color="6B6262")
linhas = [
    ("Câmbio (R$ por US$)", 5.40, '"R$" 0.00', "Premissa da equipe (cotação aproximada de nov/2026). Atualize com a cotação do dia."),
    ("Valor-hora de desenvolvimento (R$)", 80, BRL, "Premissa da equipe: média de mercado para dev júnior/pleno (PJ)."),
    ("Horizonte de análise (meses)", 12, '0" meses"', "Período usado para comparar os cenários (custo em 12 meses)."),
    ("Horas no mês (cobrança cloud)", 730, '0" h"', "Padrão AWS/Render para cobrança mensal por hora."),
    ("Fargate — US$ por vCPU-hora", 0.04048, '"US$" 0.00000', "AWS Fargate Linux/x86, us-east-1, preço de lista."),
    ("Fargate — US$ por GB-hora", 0.004445, '"US$" 0.000000', "AWS Fargate Linux/x86, us-east-1, preço de lista."),
]
p["A4"], p["B4"], p["C4"] = "Premissa", "Valor", "Fonte / observação"
for c in ("A4", "B4", "C4"):
    p[c].font, p[c].fill = CAB, FUNDO_CAB
for i, (rot, val, fmt, obs) in enumerate(linhas, start=5):
    p.cell(i, 1, rot).font = PRETO
    v = p.cell(i, 2, val); v.font, v.fill, v.number_format = AZUL, AMARELO, fmt
    p.cell(i, 3, obs).font = Font(name=FONTE, color="6B6262")
    for col in range(1, 4):
        p.cell(i, col).border = BORDA
p["A13"] = "Observações gerais"; p["A13"].font = NEGRITO
obs = [
    "Preços em US$ são estimativas de preço de lista (região us-east-1 na AWS; planos públicos de Render, Cloudflare, CloudAMQP e Sentry).",
    "Não incluem impostos (IOF ~3,5% em cartão internacional) nem descontos por reserva (Savings Plans / Reserved Instances).",
    "Volume de referência: piloto regional com ~20 hospitais, ~3 milhões de requisições HTTP/mês e telemetria a cada 30 s por veículo ativo.",
    "Esforço = horas estimadas pela equipe para levar o sistema atual até cada cenário (uma vez só, não recorrente).",
]
for i, t in enumerate(obs, start=14):
    p.cell(i, 1, "• " + t).font = PRETO
p.column_dimensions["A"].width = 38
p.column_dimensions["B"].width = 16
p.column_dimensions["C"].width = 95

CAMBIO, VHORA, MESES, HMES, VCPU, GBH = ("Premissas!$B$5", "Premissas!$B$6", "Premissas!$B$7",
                                       "Premissas!$B$8", "Premissas!$B$9", "Premissas!$B$10")


# ------------------------------------------------------------------ cenários
def cenario(nome, titulo, descricao, itens, esforco):
    ws = wb.create_sheet(nome)
    ws["A1"] = titulo; ws["A1"].font = TITULO
    ws["A2"] = descricao; ws["A2"].font = Font(name=FONTE, italic=True, color="6B6262")
    cab = ["Item", "Serviço / provedor", "Especificação", "Qtd", "US$ unit./mês", "US$/mês", "R$/mês", "Fonte / observação"]
    for c, t in enumerate(cab, start=1):
        cel = ws.cell(4, c, t); cel.font, cel.fill, cel.border = CAB, FUNDO_CAB, BORDA
        cel.alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)
    r = 5
    for item, servico, espec, qtd, unit, fonte in itens:
        ws.cell(r, 1, item); ws.cell(r, 2, servico); ws.cell(r, 3, espec)
        q = ws.cell(r, 4, qtd); q.font = AZUL
        u = ws.cell(r, 5, unit)
        u.font = PRETO if isinstance(unit, str) and unit.startswith("=") else AZUL
        u.number_format = USD
        f = ws.cell(r, 6, f"=D{r}*E{r}"); f.number_format = USD
        b = ws.cell(r, 7, f"=F{r}*{CAMBIO}"); b.number_format = BRL
        ws.cell(r, 8, fonte).font = Font(name=FONTE, color="6B6262", size=9)
        for c in range(1, 9):
            ws.cell(r, c).border = BORDA
            if ws.cell(r, c).font.name != FONTE:
                ws.cell(r, c).font = PRETO
        r += 1
    ini, fim = 5, r - 1
    ws.cell(r, 1, "Total de infraestrutura por mês").font = NEGRITO
    t1 = ws.cell(r, 6, f"=SUM(F{ini}:F{fim})"); t1.font, t1.number_format = NEGRITO, USD
    t2 = ws.cell(r, 7, f"=SUM(G{ini}:G{fim})"); t2.font, t2.number_format = NEGRITO, BRL
    for c in range(1, 9):
        ws.cell(r, c).fill = CINZA; ws.cell(r, c).border = BORDA
    linha_total_infra = r

    r += 2
    ws.cell(r, 1, "Esforço de implantação (uma vez)").font = NEGRITO
    r += 1
    for c, t in enumerate(["Atividade", "", "", "Horas", "", "", "R$", "Observação"], start=1):
        cel = ws.cell(r, c, t); cel.font, cel.fill, cel.border = CAB, FUNDO_CAB, BORDA
    r += 1
    e_ini = r
    for atv, horas, obs_ in esforco:
        ws.cell(r, 1, atv).font = PRETO
        h = ws.cell(r, 4, horas); h.font, h.number_format = AZUL, HORAS
        v = ws.cell(r, 7, f"=D{r}*{VHORA}"); v.number_format = BRL
        ws.cell(r, 8, obs_).font = Font(name=FONTE, color="6B6262", size=9)
        for c in range(1, 9):
            ws.cell(r, c).border = BORDA
        r += 1
    e_fim = r - 1
    ws.cell(r, 1, "Total de esforço").font = NEGRITO
    th = ws.cell(r, 4, f"=SUM(D{e_ini}:D{e_fim})"); th.font, th.number_format = NEGRITO, HORAS
    tv = ws.cell(r, 7, f"=SUM(G{e_ini}:G{e_fim})"); tv.font, tv.number_format = NEGRITO, BRL
    for c in range(1, 9):
        ws.cell(r, c).fill = CINZA; ws.cell(r, c).border = BORDA
    linha_total_esforco = r

    for col, w in zip("ABCDEFGH", (34, 30, 34, 7, 14, 13, 14, 70)):
        ws.column_dimensions[col].width = w
    ws.freeze_panes = "A5"
    return linha_total_infra, linha_total_esforco


FARGATE_TASK = f"={HMES}*(0.5*{VCPU}+1*{GBH})"

totais = {}
totais["A"] = cenario(
    "Cenario_A", "Cenário A — Monólito modular (Render)",
    "Mesmo deploy de hoje, com o código reorganizado por bounded context e eventos internos. Guia de transporte no Cloudflare Workers.",
    [
        ("Aplicação (monólito modular)", "Render Web Service", "Standard: 1 CPU, 2 GB RAM", 1, 25.00, "render.com/pricing — plano Standard"),
        ("Banco relacional", "Render PostgreSQL", "Basic-1gb, 1 schema por contexto", 1, 19.00, "render.com/pricing — Postgres Basic-1gb"),
        ("Função da guia de transporte", "Cloudflare Workers", "Plano Free (100 mil req/dia)", 1, 0.00, "developers.cloudflare.com/workers/platform/pricing"),
        ("CI/CD e registro de imagens", "GitHub Actions + GHCR", "Repositório público", 1, 0.00, "Gratuito para repositórios públicos"),
        ("Domínio", "Registro.br", ".com.br (R$ 40/ano ≈ US$ 0,62/mês)", 1, 0.62, "registro.br — anuidade de domínio .com.br"),
        ("Monitoramento de disponibilidade", "UptimeRobot / Grafana Cloud", "Planos gratuitos", 1, 0.00, "Planos free dos fornecedores"),
    ],
    [
        ("Reorganizar pacotes por bounded context", 40, "estoque/, alocacao/, logistica/, captacao/…"),
        ("Testes de arquitetura (ArchUnit)", 16, "Impede dependência indevida entre módulos"),
        ("Refatorar controllers para 3 camadas", 24, "Controllers deixam de acessar repositórios"),
        ("Eventos internos + outbox", 24, "ApplicationEventPublisher do Spring"),
        ("Documentação, ADRs e glossário", 8, ""),
    ],
)
totais["B"] = cenario(
    "Cenario_B", "Cenário B — Híbrido: monólito + serviços extraídos + serverless",
    "Monólito (Captação, Estoque, Requisições, Alocação) + svc-logistica e svc-telemetria extraídos (Strangler Fig), broker gerenciado e funções serverless.",
    [
        ("Monólito modular", "Render Web Service", "Standard: 1 CPU, 2 GB RAM", 1, 25.00, "render.com/pricing — plano Standard"),
        ("svc-logistica (core)", "Render Web Service", "Standard ×2 instâncias (alta disponibilidade)", 2, 25.00, "render.com/pricing — plano Standard"),
        ("svc-telemetria", "Render Web Service", "Starter: 0,5 CPU, 512 MB (serviço leve)", 1, 7.00, "render.com/pricing — plano Starter"),
        ("Bancos PostgreSQL", "Render PostgreSQL", "Basic-1gb ×3 (monólito, logística, telemetria)", 3, 19.00, "render.com/pricing — Postgres Basic-1gb"),
        ("Broker de eventos", "CloudAMQP (RabbitMQ)", "Plano dedicado de entrada", 1, 19.00, "cloudamqp.com/plans — estimativa de plano de entrada"),
        ("Cache de rotas", "Render Key Value (Redis)", "Starter 256 MB", 1, 10.00, "render.com/pricing — Key Value Starter"),
        ("Funções serverless", "Cloudflare Workers", "Plano Paid (guia + notificações)", 1, 5.00, "developers.cloudflare.com/workers/platform/pricing"),
        ("E-mail transacional", "Amazon SES", "~10 mil e-mails/mês × US$ 0,10/mil", 1, 1.00, "aws.amazon.com/ses/pricing"),
        ("Monitoramento de erros", "Sentry", "Plano Team", 1, 26.00, "sentry.io/pricing"),
        ("Domínio", "Registro.br", ".com.br", 1, 0.62, "registro.br"),
    ],
    [
        ("Tudo do cenário A (monólito modular)", 112, "Pré-requisito para extrair serviços"),
        ("Extrair svc-logistica", 120, "Rede, Dijkstra, entrega; banco próprio"),
        ("Extrair svc-telemetria", 80, "Ingestão de leituras e alertas"),
        ("Broker, outbox e consumidores idempotentes", 40, "RabbitMQ + DLQ"),
        ("Roteamento no gateway (Strangler)", 24, "Desvio gradual de rotas"),
        ("CI/CD por serviço", 24, "Pipelines independentes"),
        ("Observabilidade (logs, tracing)", 16, "Correlation-id entre serviços"),
    ],
)
totais["C"] = cenario(
    "Cenario_C", "Cenário C — Microsserviços completos na AWS",
    "Um serviço por bounded context em ECS Fargate, RDS por serviço, SNS/SQS, API Gateway, Cognito e observabilidade gerenciada.",
    [
        ("Containers dos serviços", "AWS ECS Fargate", "10 tasks de 0,5 vCPU/1 GB (7 serviços; 3 com 2 réplicas)", 10, FARGATE_TASK, "aws.amazon.com/fargate/pricing — fórmula usa a aba Premissas"),
        ("Load balancer", "AWS ALB", "1 ALB + ~1 LCU", 1, 22.27, "US$ 0,0225/h + US$ 0,008/LCU-h (us-east-1)"),
        ("API Gateway", "AWS API Gateway HTTP API", "~3 milhões de req/mês × US$ 1,00/milhão", 3, 1.00, "aws.amazon.com/api-gateway/pricing"),
        ("Bancos dos serviços de suporte", "AWS RDS PostgreSQL", "db.t4g.micro Single-AZ + 20 GB gp3", 4, 13.98, "US$ 0,016/h + US$ 0,115/GB-mês"),
        ("Bancos dos serviços core", "AWS RDS PostgreSQL", "db.t4g.small Multi-AZ + 20 GB gp3", 2, 51.32, "US$ 0,064/h (Multi-AZ) + US$ 0,23/GB-mês"),
        ("Séries temporais (telemetria)", "AWS DynamoDB", "On-demand, retenção 90 dias", 1, 10.00, "Estimativa de leitura/escrita on-demand"),
        ("Mensageria", "AWS SNS + SQS", "Tópicos por evento + filas com DLQ", 1, 2.00, "Volume baixo; primeiro milhão de req grátis"),
        ("Cache", "AWS ElastiCache (Redis)", "cache.t4g.micro", 1, 11.68, "US$ 0,016/h"),
        ("NAT Gateway", "AWS VPC", "1 NAT + ~10 GB processados", 1, 33.30, "US$ 0,045/h + US$ 0,045/GB — custo fixo muitas vezes esquecido"),
        ("IPv4 públicos", "AWS VPC", "3 endereços (ALB ×2, NAT)", 3, 3.65, "US$ 0,005/h por IPv4 público"),
        ("Logs e métricas", "AWS CloudWatch", "~20 GB de logs + alarmes", 1, 20.00, "US$ 0,50/GB ingerido + métricas"),
        ("Tracing", "AWS X-Ray", "Amostragem de 5%", 1, 5.00, "aws.amazon.com/xray/pricing"),
        ("Segredos", "AWS Secrets Manager", "12 segredos", 12, 0.40, "US$ 0,40/segredo/mês"),
        ("Registro de imagens", "AWS ECR", "~10 GB", 1, 1.00, "US$ 0,10/GB-mês"),
        ("DNS", "AWS Route 53", "1 zona + consultas", 1, 1.00, "US$ 0,50/zona + consultas"),
        ("Armazenamento de objetos", "AWS S3", "~50 GB (relatórios, backups)", 1, 1.15, "US$ 0,023/GB-mês"),
        ("Backup", "AWS Backup", "Snapshots dos RDS", 1, 5.00, "Estimativa"),
        ("Identidade", "AWS Cognito", "Plano Lite, até 10 mil usuários ativos", 1, 0.00, "Franquia gratuita do plano Lite"),
        ("Funções serverless", "AWS Lambda", "Guia + notificações", 1, 0.00, "Coberto pela franquia gratuita (1 milhão req/mês)"),
        ("Suporte da AWS", "AWS Support", "Plano Developer", 1, 29.00, "aws.amazon.com/premiumsupport/pricing"),
    ],
    [
        ("Tudo do cenário B", 416, "Inclui monólito modular e as duas primeiras extrações"),
        ("Extrair svc-captacao", 60, ""),
        ("Extrair svc-estoque", 80, "API de reserva (Open Host Service)"),
        ("Extrair svc-requisicoes", 60, ""),
        ("Extrair svc-alocacao (core)", 100, "Agregado Alocacao + eventos"),
        ("svc-indicadores com read model (CQRS)", 60, ""),
        ("Infraestrutura como código (Terraform)", 80, "VPC, ECS, RDS, IAM"),
        ("Migração de dados por serviço", 60, "Schema → banco próprio"),
        ("Testes de contrato entre serviços", 40, "Pact / JSON Schema"),
        ("Segurança e IAM", 24, "Least privilege por serviço"),
    ],
)

# ------------------------------------------------------------------ Resumo
r = wb.create_sheet("Resumo", 1)
r["A1"] = "Resumo comparativo dos cenários"; r["A1"].font = TITULO
r["A2"] = "Valores em verde vêm das abas de cada cenário; tudo recalcula a partir da aba Premissas."
r["A2"].font = Font(name=FONTE, italic=True, color="6B6262")
cab = ["Indicador", "A — Monólito modular", "B — Híbrido", "C — Microsserviços AWS"]
for c, t in enumerate(cab, start=1):
    cel = r.cell(4, c, t); cel.font, cel.fill, cel.border = CAB, FUNDO_CAB, BORDA
    cel.alignment = Alignment(horizontal="center", wrap_text=True)
linhas_resumo = [
    ("Infraestrutura (US$/mês)", lambda s, li, le: f"={s}!F{li}", USD, VERDE),
    ("Infraestrutura (R$/mês)", lambda s, li, le: f"={s}!G{li}", BRL, VERDE),
    ("Infraestrutura no horizonte (R$)", lambda s, li, le: f"={s}!G{li}*{MESES}", BRL0, PRETO),
    ("Esforço de implantação (horas)", lambda s, li, le: f"={s}!D{le}", HORAS, VERDE),
    ("Esforço de implantação (R$)", lambda s, li, le: f"={s}!G{le}", BRL0, VERDE),
    ("Custo total no horizonte (R$)", None, BRL0, NEGRITO),
]
for i, (rot, fn, fmt, fonte) in enumerate(linhas_resumo, start=5):
    r.cell(i, 1, rot).font = NEGRITO if fn is None else PRETO
    for j, k in enumerate("ABC", start=2):
        li, le = totais[k]
        col = "BCD"[j - 2]
        valor = f"={col}7+{col}9" if fn is None else fn(f"Cenario_{k}", li, le)
        cel = r.cell(i, j, valor); cel.number_format, cel.font, cel.border = fmt, fonte, BORDA
    r.cell(i, 1).border = BORDA
r.cell(11, 1, "Multiplicador de custo vs. cenário A").font = PRETO
for j, col in enumerate("BCD", start=2):
    cel = r.cell(11, j, f"=IF($B$10=0,0,{col}10/$B$10)"); cel.number_format = '0.0"x"'; cel.border = BORDA
r.cell(11, 1).border = BORDA

qual = [
    ("Complexidade operacional", "Baixa", "Média", "Alta"),
    ("Isolamento de falhas", "Baixo", "Médio (core logístico isolado)", "Alto"),
    ("Escala independente", "Não", "Logística e telemetria", "Todos os contextos"),
    ("Tempo até produção", "~3 semanas", "~3 meses", "~7 meses"),
    ("Recomendação", "Fase 1", "ALVO (12 meses)", "Horizonte, por gatilho"),
]
for i, (rot, a, b, c) in enumerate(qual, start=13):
    for j, t in enumerate((rot, a, b, c), start=1):
        cel = r.cell(i, j, t); cel.font = PRETO; cel.border = BORDA
        cel.alignment = Alignment(wrap_text=True, vertical="center")
r.cell(17, 3).fill = PatternFill("solid", fgColor="FDECEA"); r.cell(17, 3).font = Font(name=FONTE, bold=True, color="B3121F")
r.cell(16, 1).comment = Comment("Estimativa considerando 2 a 3 pessoas com dedicação parcial (projeto acadêmico).", "Equipe HemoFlow")
for col, w in zip("ABCD", (40, 24, 30, 26)):
    r.column_dimensions[col].width = w

graf = BarChart()
graf.type = "col"
graf.title = "Custo total no horizonte (R$)"
graf.y_axis.title = "R$"
graf.add_data(Reference(r, min_col=2, max_col=4, min_row=10, max_row=10), from_rows=True, titles_from_data=False)
graf.set_categories(Reference(r, min_col=2, max_col=4, min_row=4, max_row=4))
graf.legend = None
graf.roundedCorners = False
graf.title.overlay = False
graf.y_axis.title.overlay = False
graf.x_axis.delete = False
graf.y_axis.delete = False
graf.y_axis.numFmt = '"R$" #,##0'
graf.height, graf.width = 8, 16
r.add_chart(graf, "F4")

wb.calculation.fullCalcOnLoad = True
DESTINO.parent.mkdir(parents=True, exist_ok=True)
wb.save(DESTINO)
print("ok", DESTINO)
