#ifndef ROTEADOR_VIZINHO_HPP
#define ROTEADOR_VIZINHO_HPP
#include <cstdint>
#include <string>
#include <vector>
struct ArestaRota { std::string destino; int peso; };
struct ResultadoRota { std::vector<std::string> rota; std::int64_t custo; bool encontrado; };
ResultadoRota vizinho_mais_proximo(const std::vector<std::string>& nos,
 const std::vector<std::vector<ArestaRota>>& adj, const std::string& origem,const std::string& destino);
#endif
