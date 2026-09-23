import React from 'react';
import { useApp } from '../../context/AppContext';
import { Radar, MessageSquare, AlertTriangle, Settings } from 'lucide-react';

export const TacticalBottomNav: React.FC = () => {
  const { activeTab, setActiveTab, conversations, sosAlerts } = useApp();

  const totalUnread = conversations.reduce((acc, c) => acc + c.unreadCount, 0);
  const unacknowledgedSos = sosAlerts.filter(a => !a.acknowledged).length;

  const tabs = [
    {
      id: 'radar' as const,
      label: 'Radar',
      icon: Radar,
      badge: null
    },
    {
      id: 'chats' as const,
      label: 'Chats',
      icon: MessageSquare,
      badge: totalUnread > 0 ? totalUnread : null,
      badgeColor: 'bg-[#F59E0B] text-black'
    },
    {
      id: 'sos' as const,
      label: 'SOS',
      icon: AlertTriangle,
      badge: unacknowledgedSos > 0 ? unacknowledgedSos : null,
      badgeColor: 'bg-[#EF4444] text-white animate-pulse'
    },
    {
      id: 'settings' as const,
      label: 'Settings',
      icon: Settings,
      badge: null
    }
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#111822]/98 backdrop-blur-lg border-t border-[#26354A] px-2 py-1.5 shadow-2xl">
      <div className="max-w-md mx-auto grid grid-cols-4 gap-1">
        {tabs.map(tab => {
          const isActive = activeTab === tab.id;
          const Icon = tab.icon;

          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`relative flex flex-col items-center justify-center py-1.5 px-2 rounded-xl transition-all cursor-pointer ${
                isActive
                  ? 'bg-[#1B2636] text-[#F59E0B]'
                  : 'text-[#9CA3AF] hover:text-[#F9FAFB] hover:bg-[#1B2636]/50'
              }`}
            >
              <div className="relative">
                <Icon className={`w-5 h-5 ${isActive ? 'text-[#F59E0B]' : 'text-[#9CA3AF]'}`} />
                {tab.badge !== null && (
                  <span
                    className={`absolute -top-1.5 -right-2.5 min-w-[16px] h-4 px-1 rounded-full text-[10px] font-mono-tactical font-black flex items-center justify-center ${
                      tab.badgeColor || 'bg-[#F59E0B] text-black'
                    }`}
                  >
                    {tab.badge}
                  </span>
                )}
              </div>
              <span
                className={`text-[10px] font-mono-tactical tracking-wider mt-0.5 ${
                  isActive ? 'font-bold text-[#F59E0B]' : 'font-medium text-[#9CA3AF]'
                }`}
              >
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
