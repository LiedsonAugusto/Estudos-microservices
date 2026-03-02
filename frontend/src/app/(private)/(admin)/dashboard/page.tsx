import GraphicBar from '@/components/admin/GraphicBar';
import StatisticsCard from '@/components/admin/StatisticsCard';
import React from 'react';

const Page = () => {
    return (
        <div className='flex flex-col gap-10'>  
            <div className='flex justify-around items-center m-10'>
                <StatisticsCard title='Total de agendamentos hoje:' value={150} />
                <StatisticsCard title='Total de cancelamentos hoje:' value={150} />
                <StatisticsCard title='Total de confirmados hoje:' value={150} />
            </div>
            <div>
                <GraphicBar />
            </div>
            
        </div>
        
    );
}

export default Page;
