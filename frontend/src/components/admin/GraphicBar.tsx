'use client';

import React, { useMemo } from 'react';
import ReactECharts from 'echarts-for-react';

function getLast7DaysLabels() {
  const days: string[] = [];

  for (let i = 6; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);

    const label = d.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit'
    });

    days.push(label);
  }

  return days;
}

export default function GraphicBar() {

  const option = useMemo(() => {
    const labels = getLast7DaysLabels();

    // dados simulados
    const concluidos = [18, 22, 20, 25, 19, 30, 27];
    const cancelados = [3, 5, 2, 4, 6, 3, 4];

    return {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['Concluídos', 'Cancelados']
      },
      toolbox: {
        show: true,
        feature: {
          dataView: { show: true, readOnly: true },
          magicType: { show: true, type: ['line', 'bar'] },
          restore: { show: true },
          saveAsImage: { show: true }
        }
      },
      xAxis: {
        type: 'category',
        data: labels
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: 'Concluídos',
          type: 'bar',
          data: concluidos,
          markPoint: {
            data: [
              { type: 'max', name: 'Máx' },
              { type: 'min', name: 'Mín' }
            ]
          },
          markLine: {
            data: [{ type: 'average', name: 'Média' }]
          }
        },
        {
          name: 'Cancelados',
          type: 'bar',
          data: cancelados,
                itemStyle: {
            color: '#727272' // vermelho
            },
          markPoint: {
            data: [
              { type: 'max', name: 'Máx' },
              { type: 'min', name: 'Mín' }
            ]
          },
          markLine: {
            data: [{ type: 'average', name: 'Média' }]
          }
        }
      ]
    };
  }, []);

  return (
    <ReactECharts
        className=''
      option={option}
      style={{ height: "50vh", width: '100%' }}
    />
  );
}