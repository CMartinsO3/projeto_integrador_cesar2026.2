import { apiRequest } from "./api-client.js";
import { toast } from "./ui.js";

const mapElement = document.getElementById("mapa");
if (mapElement && window.L) {
  const map = window.L.map("mapa").setView([-8.05,-34.9],11);
  window.L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",{attribution:"&copy; OpenStreetMap contributors"}).addTo(map);
  const origin = document.getElementById("routeOrigin");
  const destination = document.getElementById("routeDestination");
  let networkLayer = window.L.layerGroup().addTo(map);
  let routeLayer = null;

  apiRequest("/api/v1/topologia").then(topology => {
    const points = topology.nos.filter(point => Number.isFinite(point.latitude) && Number.isFinite(point.longitude));
    topology.ligacoes.forEach(connection => {
      const from = points.find(point => point.id === connection.origemId);
      const to = points.find(point => point.id === connection.destinoId);
      if (from && to) window.L.polyline([[from.latitude,from.longitude],[to.latitude,to.longitude]],{color:"#78909c",weight:2,opacity:.55}).addTo(networkLayer);
    });
    points.forEach(point => window.L.circleMarker([point.latitude,point.longitude],{radius:6,color:"#145c57",fillColor:"#fff",fillOpacity:1,weight:2}).bindPopup(`${point.nome} (${point.codigo})`).addTo(networkLayer));
    [origin,destination].forEach(select => {
      select.replaceChildren(new Option("Selecione um ponto",""));
      points.slice().sort((a,b) => a.nome.localeCompare(b.nome,"pt-BR")).forEach(point => select.add(new Option(`${point.nome} (${point.codigo})`,point.id)));
      select.disabled = false;
    });
    document.getElementById("routeSubmit").disabled = false;
    if (points.length) map.fitBounds(window.L.latLngBounds(points.map(point => [point.latitude,point.longitude])),{padding:[30,30],maxZoom:13});
  }).catch(error => toast(error.message,"error"));

  document.getElementById("routeForm").addEventListener("submit", async event => {
    event.preventDefault();
    if (!origin.value || !destination.value) {
      toast("Selecione origem e destino.","warning");
      return;
    }
    if (routeLayer) map.removeLayer(routeLayer);
    try {
      const params = new URLSearchParams({origemId:origin.value,destinoId:destination.value});
      const route = await apiRequest(`/api/v1/rotas/vizinho-mais-proximo?${params}`);
      const coordinates = route.rota.filter(point => Number.isFinite(point.latitude) && Number.isFinite(point.longitude)).map(point => [point.latitude,point.longitude]);
      routeLayer = window.L.layerGroup().addTo(map);
      window.L.polyline(coordinates,{color:route.alertaCadeiaFria?"#a82d24":"#176b68",weight:5,opacity:.9,dashArray:route.encontrada?null:"8 8"}).addTo(routeLayer);
      route.rota.forEach((point,index) => window.L.marker(coordinates[index]).bindPopup(`${index+1}. ${point.nome}`).addTo(routeLayer));
      document.getElementById("routeTime").textContent = `${route.tempoTotalMinutos} min`;
      document.getElementById("routeDistance").textContent = `${Number(route.distanciaTotalKm).toFixed(2)} km`;
      const warning = document.getElementById("routeColdWarning");
      warning.hidden = !route.alertaCadeiaFria;
      warning.textContent = route.alertaCadeiaFria ? `Rota acima do limite didático de ${route.limiteCadeiaFriaMinutos} min.` : "";
      const stops = document.getElementById("routeStops");
      stops.replaceChildren(...route.rota.map((point,index) => {
        const item = document.createElement("li");
        item.textContent = `${index+1}. ${point.nome} (${point.codigo})`;
        return item;
      }));
      document.getElementById("routeResult").hidden = false;
      map.fitBounds(window.L.latLngBounds(coordinates),{padding:[40,40],maxZoom:14});
    } catch (error) {
      toast(error.message,"error");
    }
  });
}
