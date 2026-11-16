-- Dados de exemplo carregados ao iniciar a aplicação

INSERT INTO servico (nome, duracao_minutos, preco) VALUES
    ('Corte de cabelo', 30, 35.00),
    ('Coloração', 90, 120.00),
    ('Design de sobrancelhas', 45, 50.00),
    ('Limpeza de pele', 60, 90.00);

INSERT INTO profissional (nome, especialidade, inicio_expediente, fim_expediente, inicio_intervalo, fim_intervalo) VALUES
    ('Ana Lima', 'Cabeleireira', '08:00:00', '18:00:00', '12:00:00', '13:00:00'),
    ('Bruno Costa', 'Esteticista', '09:00:00', '17:00:00', '12:30:00', '13:30:00');

INSERT INTO profissional_dias_atendimento (profissional_id, dias_atendimento) VALUES
    (1, 'MONDAY'), (1, 'TUESDAY'), (1, 'WEDNESDAY'), (1, 'THURSDAY'), (1, 'FRIDAY'),
    (2, 'TUESDAY'), (2, 'THURSDAY'), (2, 'SATURDAY');
