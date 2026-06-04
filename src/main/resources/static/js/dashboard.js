/* ===================================================================
   SOC DASHBOARD CHARTS - SECURE ENTERPRISE NETWORK MONITOR
   =================================================================== */

document.addEventListener("DOMContentLoaded", function () {
    // Read chart data from the DOM element
    const dataContainer = document.getElementById("charts-data");
    if (!dataContainer) return;

    const onlineCount = parseInt(dataContainer.getAttribute("data-online") || "0");
    const offlineCount = parseInt(dataContainer.getAttribute("data-offline") || "0");
    
    const criticalCount = parseInt(dataContainer.getAttribute("data-critical") || "0");
    const highCount = parseInt(dataContainer.getAttribute("data-high") || "0");
    const mediumCount = parseInt(dataContainer.getAttribute("data-medium") || "0");
    const lowCount = parseInt(dataContainer.getAttribute("data-low") || "0");

    // 1. Device Status Chart (Doughnut)
    const deviceCtx = document.getElementById("deviceStatusChart");
    if (deviceCtx) {
        new Chart(deviceCtx, {
            type: "doughnut",
            data: {
                labels: ["Online", "Offline"],
                datasets: [{
                    data: [onlineCount, offlineCount],
                    backgroundColor: ["#00e676", "#3f4756"],
                    borderColor: "#141824",
                    borderWidth: 3,
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: "bottom",
                        labels: {
                            color: "#94a3b8",
                            font: {
                                family: "Outfit",
                                size: 12
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                let label = context.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                label += context.raw;
                                return label;
                            }
                        }
                    }
                },
                cutout: "75%"
            }
        });
    }

    // 2. Alert Severity Chart (Bar)
    const severityCtx = document.getElementById("alertSeverityChart");
    if (severityCtx) {
        new Chart(severityCtx, {
            type: "bar",
            data: {
                labels: ["Critical", "High", "Medium", "Low"],
                datasets: [{
                    label: "Incidents Count",
                    data: [criticalCount, highCount, mediumCount, lowCount],
                    backgroundColor: [
                        "#ff1744", // Critical
                        "#ff9100", // High
                        "#2979ff", // Medium
                        "#00e5ff"  // Low
                    ],
                    borderColor: "transparent",
                    borderRadius: 6,
                    borderSkipped: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        },
                        ticks: {
                            color: "#94a3b8",
                            font: {
                                family: "Outfit"
                            }
                        }
                    },
                    y: {
                        grid: {
                            color: "#232c3f"
                        },
                        ticks: {
                            color: "#94a3b8",
                            font: {
                                family: "Outfit"
                            },
                            stepSize: 1,
                            precision: 0
                        }
                    }
                }
            }
        });
    }
});
